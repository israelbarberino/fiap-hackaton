package br.com.fiap.susagenda.domain.patient;

import java.time.Instant;
import java.util.UUID;

public record Patient(
        String id,
        String name,
        String cpf,
        String email,
        String phone,
        String address,
        boolean whatsappEnabled,
        Instant createdAt,
        Instant updatedAt) {

    public static Patient create(String name, String cpf, String email, String phone, String address,
            boolean whatsappEnabled) {
        Instant now = Instant.now();
        return new Patient(UUID.randomUUID().toString(), name, cpf, email, phone, address, whatsappEnabled, now, now);
    }

    public static Patient createForUser(String userId, String name, String cpf, String email, String phone,
            String address, boolean whatsappEnabled) {
        Instant now = Instant.now();
        return new Patient(userId, name, cpf, email, phone, address, whatsappEnabled, now, now);
    }
}
