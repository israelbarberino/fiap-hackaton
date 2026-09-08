package br.com.fiap.susagenda.api.patient;

import br.com.fiap.susagenda.application.patient.PatientRepository;
import br.com.fiap.susagenda.domain.patient.Patient;
import br.com.fiap.susagenda.application.auth.AuthorizationService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;

@Path("/api/v1/patients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatientResource {
    private final PatientRepository patientRepository;
    private final AuthorizationService authorizationService;

    public PatientResource(PatientRepository patientRepository, AuthorizationService authorizationService) {
        this.patientRepository = patientRepository;
        this.authorizationService = authorizationService;
    }

    @POST
    public Response create(CreatePatientRequest request, @Context ContainerRequestContext context) {
        if (!authorizationService.hasAnyRole(context, "PATIENT", "HEALTH_AGENT", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("FORBIDDEN", "Você não tem permissão para cadastrar pacientes."))
                    .build();
        }
        if (request.name() == null || request.name().isBlank() || request.cpf() == null || request.cpf().isBlank()
                || request.email() == null || request.email().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_PATIENT_REQUEST", "Nome, CPF e e-mail são obrigatórios."))
                    .build();
        }
        if (patientRepository.findByCpf(request.cpf()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("PATIENT_ALREADY_EXISTS", "Já existe paciente com este CPF."))
                    .build();
        }
        String userRole = (String) context.getProperty("userRole");
        String userId = (String) context.getProperty("userId");
        Patient patient = "PATIENT".equals(userRole) && userId != null
            ? Patient.createForUser(userId, request.name(), request.cpf(), request.email(), request.phone(),
                request.address(), request.whatsappEnabled())
            : Patient.create(request.name(), request.cpf(), request.email(), request.phone(), request.address(),
                request.whatsappEnabled());
        patientRepository.save(patient);
        return Response.status(Response.Status.CREATED).entity(patient).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") String id, @Context ContainerRequestContext context) {
        if (!authorizationService.isOwnResourceOrRole(context, id, "HEALTH_AGENT", "DOCTOR", "ADMIN")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("FORBIDDEN", "Você não tem permissão para consultar este paciente."))
                    .build();
        }
        return patientRepository.findById(id)
                .map(patient -> Response.ok(patient).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("PATIENT_NOT_FOUND", "Paciente não encontrado."))
                        .build());
    }

    public record CreatePatientRequest(
            String name,
            String cpf,
            String email,
            String phone,
            String address,
            boolean whatsappEnabled) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
