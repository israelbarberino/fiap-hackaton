package br.com.fiap.susagenda.api.appointment;

import br.com.fiap.susagenda.application.appointment.AppointmentBookingService;
import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import br.com.fiap.susagenda.domain.appointment.ScheduleRuleException;
import br.com.fiap.susagenda.application.appointment.AppointmentAvailabilityService;
import br.com.fiap.susagenda.application.appointment.AppointmentSlotRepository;
import br.com.fiap.susagenda.application.auth.AuthorizationService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Path("/api/v1/appointments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AppointmentResource {
    private final AppointmentBookingService bookingService;
        private final AppointmentSlotRepository slotRepository;
        private final AuthorizationService authorizationService;

        public AppointmentResource(AppointmentBookingService bookingService, AppointmentSlotRepository slotRepository,
                        AuthorizationService authorizationService) {
        this.bookingService = bookingService;
                this.slotRepository = slotRepository;
                this.authorizationService = authorizationService;
    }

    @POST
        public Response create(CreateAppointmentRequest request, @Context ContainerRequestContext context) {
                if (!authorizationService.isOwnResourceOrRole(context, request.patientId(), "HEALTH_AGENT", "ADMIN")) {
                        return Response.status(Response.Status.FORBIDDEN)
                                        .entity(new ErrorResponse("FORBIDDEN", "Você não pode agendar para este paciente."))
                                        .build();
                }
        try {
            var appointment = bookingService.create(
                    request.patientId(),
                    request.professionalId(),
                    request.unitId(),
                    request.type(),
                    new AppointmentSlot(LocalDate.parse(request.date()), LocalTime.parse(request.time())));
            return Response.status(Response.Status.CREATED)
                    .entity(appointment)
                    .build();
        } catch (AppointmentBookingService.AppointmentConflictException exception) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new AppointmentConflictResponse(
                            "APPOINTMENT_TIME_UNAVAILABLE",
                            exception.getMessage(),
                            exception.suggestedSlots()))
                    .build();
        } catch (ScheduleRuleException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(exception.code(), exception.getMessage()))
                    .build();
        } catch (RuntimeException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_APPOINTMENT_REQUEST", "Dados de agendamento inválidos."))
                    .build();
        }
    }

    @GET
    @Path("/availability")
    public Response availability(@QueryParam("professionalId") String professionalId,
            @QueryParam("unitId") String unitId, @QueryParam("date") String date,
            @QueryParam("time") String time) {
        try {
            var occupied = slotRepository.findOccupiedSlots(professionalId, unitId);
            var slots = new AppointmentAvailabilityService().suggestAvailableSlots(
                    LocalDate.parse(date), LocalTime.parse(time), occupied);
            return Response.ok(slots).build();
        } catch (ScheduleRuleException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(exception.code(), exception.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") String id, @Context ContainerRequestContext context) {
        try {
            var appointment = bookingService.findById(id);
            if (!authorizationService.isOwnResourceOrRole(context, appointment.patientId(), "HEALTH_AGENT", "DOCTOR",
                    "ADMIN")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("FORBIDDEN", "Você não tem permissão para consultar este agendamento."))
                        .build();
            }
            return Response.ok(appointment).build();
        } catch (AppointmentBookingService.AppointmentNotFoundException exception) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("APPOINTMENT_NOT_FOUND", exception.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/reschedule")
    public Response reschedule(@PathParam("id") String id, RescheduleRequest request,
            @Context ContainerRequestContext context) {
        try {
            var current = bookingService.findById(id);
            if (!authorizationService.isOwnResourceOrRole(context, current.patientId(), "HEALTH_AGENT", "ADMIN")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("FORBIDDEN", "Você não pode reagendar este agendamento."))
                        .build();
            }
            var appointment = bookingService.reschedule(id,
                    new AppointmentSlot(LocalDate.parse(request.date()), LocalTime.parse(request.time())));
            return Response.ok(appointment).build();
        } catch (AppointmentBookingService.AppointmentConflictException exception) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new AppointmentConflictResponse("APPOINTMENT_TIME_UNAVAILABLE", exception.getMessage(),
                            exception.suggestedSlots()))
                    .build();
        } catch (AppointmentBookingService.AppointmentNotFoundException exception) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("APPOINTMENT_NOT_FOUND", exception.getMessage()))
                    .build();
        } catch (ScheduleRuleException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(exception.code(), exception.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/{id}/cancel")
    public Response cancel(@PathParam("id") String id, CancelRequest request,
            @Context ContainerRequestContext context) {
        try {
            var current = bookingService.findById(id);
            if (!authorizationService.isOwnResourceOrRole(context, current.patientId(), "HEALTH_AGENT", "ADMIN")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("FORBIDDEN", "Você não pode cancelar este agendamento."))
                        .build();
            }
            return Response.ok(bookingService.cancel(id, request.reason())).build();
        } catch (AppointmentBookingService.AppointmentNotFoundException exception) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("APPOINTMENT_NOT_FOUND", exception.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}/history")
    public Response history(@PathParam("id") String id, @Context ContainerRequestContext context) {
        try {
            var current = bookingService.findById(id);
            if (!authorizationService.isOwnResourceOrRole(context, current.patientId(), "HEALTH_AGENT", "DOCTOR",
                    "ADMIN")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new ErrorResponse("FORBIDDEN", "Você não tem permissão para consultar este histórico."))
                        .build();
            }
            return Response.ok(bookingService.history(id)).build();
        } catch (AppointmentBookingService.AppointmentNotFoundException exception) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("APPOINTMENT_NOT_FOUND", exception.getMessage()))
                    .build();
        }
    }

    public record CreateAppointmentRequest(
            String patientId,
            String professionalId,
            String unitId,
            String type,
            String date,
            String time) {
    }

    public record AppointmentConflictResponse(
            String code,
            String message,
            List<AppointmentSlot> suggestedSlots) {
    }

    public record ErrorResponse(String code, String message) {
    }

        public record RescheduleRequest(String date, String time) {
        }

        public record CancelRequest(String reason) {
        }

}
