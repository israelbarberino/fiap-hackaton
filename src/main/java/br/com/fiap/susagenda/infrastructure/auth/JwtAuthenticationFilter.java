package br.com.fiap.susagenda.infrastructure.auth;

import br.com.fiap.susagenda.application.auth.JwtService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    private final JwtService jwtService;
    private final boolean enabled;

    @Inject
    public JwtAuthenticationFilter(JwtService jwtService,
            @ConfigProperty(name = "sus-agenda.auth.enabled", defaultValue = "true") boolean enabled) {
        this.jwtService = jwtService;
        this.enabled = enabled;
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String path = context.getUriInfo().getPath().replaceFirst("^/", "");
        if (!enabled || path.startsWith("api/v1/auth") || path.startsWith("openapi") || path.startsWith("swagger-ui")) {
            if (!enabled) {
                context.setProperty("auth.disabled", Boolean.TRUE);
            }
            return;
        }

        String authorization = context.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            reject(context, "AUTHENTICATION_REQUIRED", "Autenticação obrigatória.");
            return;
        }

        Map<String, String> claims = jwtService.verify(authorization.substring("Bearer ".length()).trim());
        if (claims.isEmpty()) {
            reject(context, "INVALID_TOKEN", "Token inválido ou expirado.");
            return;
        }
        context.setProperty("userId", claims.get("sub"));
        context.setProperty("userRole", claims.get("role"));
    }

    private void reject(ContainerRequestContext context, String code, String message) {
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(code, message))
                .build());
    }

    public record ErrorResponse(String code, String message) {
    }
}