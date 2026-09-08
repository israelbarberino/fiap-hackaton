package br.com.fiap.susagenda.domain.user;

import java.time.Instant;
import java.util.UUID;

public record User(
        String id,
        String name,
        String cpf,
        String email,
        String passwordHash,
        UserRole role,
        boolean active,
        Instant createdAt) {

    public static User create(String name, String cpf, String email, String passwordHash, UserRole role) {
        return new User(UUID.randomUUID().toString(), name, cpf, email, passwordHash, role, true, Instant.now());
    }
}
