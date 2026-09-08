package br.com.fiap.susagenda.domain.appointment;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record Appointment(
        String id,
        String patientId,
        String professionalId,
        String unitId,
        String type,
        LocalDate date,
        LocalTime time,
        AppointmentStatus status,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt) {

    public static Appointment create(String patientId, String professionalId, String unitId, String type,
            LocalDate date, LocalTime time) {
        Instant now = Instant.now();
        return new Appointment(UUID.randomUUID().toString(), patientId, professionalId, unitId, type, date, time,
                AppointmentStatus.AGENDADO, null, now, now);
    }

    public Appointment reschedule(LocalDate newDate, LocalTime newTime) {
        return new Appointment(id, patientId, professionalId, unitId, type, newDate, newTime,
                AppointmentStatus.REAGENDADO, cancellationReason, createdAt, Instant.now());
    }

    public Appointment cancel(String reason) {
        return new Appointment(id, patientId, professionalId, unitId, type, date, time,
                AppointmentStatus.CANCELADO, reason, createdAt, Instant.now());
    }
}
