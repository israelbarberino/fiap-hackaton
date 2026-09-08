package br.com.fiap.susagenda.api.auth;

import br.com.fiap.susagenda.application.auth.AuthService;
import br.com.fiap.susagenda.domain.user.User;
import br.com.fiap.susagenda.domain.user.UserRole;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        try {
            UserRole requestedRole = request.role() == null ? UserRole.PATIENT : UserRole.valueOf(request.role());
            if (requestedRole != UserRole.PATIENT) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse("PUBLIC_REGISTRATION_FORBIDDEN",
                    "Cadastro público disponível apenas para pacientes."))
                .build();
            }
            User user = authService.register(request.name(), request.cpf(), request.email(), request.password(),
                requestedRole);
            return Response.status(Response.Status.CREATED)
                    .entity(new UserResponse(user.id(), user.name(), user.email(), user.role()))
                    .build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("INVALID_USER_REQUEST", "Perfil de usuário inválido."))
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            return Response.ok(new LoginResponse(authService.login(request.login(), request.password()), "Bearer"))
                    .build();
        } catch (AuthService.AuthenticationException exception) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("INVALID_CREDENTIALS", exception.getMessage()))
                    .build();
        }
    }

    public record RegisterRequest(String name, String cpf, String email, String password, String role) {
    }

    public record LoginRequest(String login, String password) {
    }

    public record LoginResponse(String accessToken, String tokenType) {
    }

    public record UserResponse(String id, String name, String email, UserRole role) {
    }

    public record ErrorResponse(String code, String message) {
    }
}
