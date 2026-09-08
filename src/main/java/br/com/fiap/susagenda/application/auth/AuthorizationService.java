package br.com.fiap.susagenda.application.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.Set;

@ApplicationScoped
public class AuthorizationService {
    public boolean hasAnyRole(ContainerRequestContext context, String... roles) {
        if (Boolean.TRUE.equals(context.getProperty("auth.disabled"))) {
            return true;
        }
        Object role = context.getProperty("userRole");
        return role != null && Set.of(roles).contains(role.toString());
    }

    public boolean isOwnResourceOrRole(ContainerRequestContext context, String resourceUserId, String... roles) {
        if (Boolean.TRUE.equals(context.getProperty("auth.disabled"))) {
            return true;
        }
        return hasAnyRole(context, roles)
                || resourceUserId != null && resourceUserId.equals(context.getProperty("userId"));
    }
}
