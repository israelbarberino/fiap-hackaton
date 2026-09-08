package br.com.fiap.susagenda.domain.medicalrecord;

import java.time.Instant;
import java.util.UUID;

public record MedicalRecordEntry(
        String entryId,
        String patientId,
        String professionalId,
        String appointmentId,
        String evolutionType,
        String description,
        String observations,
        Instant createdAt) {

    public static MedicalRecordEntry create(String patientId, String professionalId, String appointmentId,
            String evolutionType, String description, String observations) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Descrição da evolução é obrigatória.");
        }
        return new MedicalRecordEntry(UUID.randomUUID().toString(), patientId, professionalId, appointmentId,
                evolutionType, description, observations, Instant.now());
    }
}
