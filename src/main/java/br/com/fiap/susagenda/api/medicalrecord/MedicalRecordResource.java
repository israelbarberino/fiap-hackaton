package br.com.fiap.susagenda.api.medicalrecord;

import br.com.fiap.susagenda.application.auth.AuthorizationService;
import br.com.fiap.susagenda.application.medicalrecord.MedicalRecordRepository;
import br.com.fiap.susagenda.domain.medicalrecord.MedicalRecordEntry;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/patients/{patientId}/medical-record")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MedicalRecordResource {
    private final MedicalRecordRepository repository;
    private final AuthorizationService authorizationService;

    public MedicalRecordResource(MedicalRecordRepository repository, AuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    @POST
    @Path("/entries")
    public Response append(@PathParam("patientId") String patientId, CreateEntryRequest request,
            @Context ContainerRequestContext context) {
        if (!authorizationService.hasAnyRole(context, "DOCTOR", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("FORBIDDEN", "Somente médicos ou administradores podem registrar evolução."))
                    .build();
        }
        try {
            MedicalRecordEntry entry = repository.append(MedicalRecordEntry.create(
                    patientId,
                    request.professionalId(),
                    request.appointmentId(),
                    request.evolutionType(),
                    request.description(),
                    request.observations()));
            return Response.status(Response.Status.CREATED).entity(entry).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_MEDICAL_RECORD_ENTRY", exception.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/history")
    public Response history(@PathParam("patientId") String patientId, @Context ContainerRequestContext context) {
        if (!authorizationService.isOwnResourceOrRole(context, patientId, "DOCTOR", "HEALTH_AGENT", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("FORBIDDEN", "Você não tem permissão para consultar este prontuário."))
                    .build();
        }
        return Response.ok(repository.findByPatientId(patientId)).build();
    }

    public record CreateEntryRequest(
            String professionalId,
            String appointmentId,
            String evolutionType,
            String description,
            String observations) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
