package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.Appointment;

import java.util.List;

public interface AppointmentHistoryRepository {
    void append(String appointmentId, Appointment appointment, String action);

    List<AppointmentHistoryEntry> findByAppointmentId(String appointmentId);

    record AppointmentHistoryEntry(String appointmentId, Appointment appointment, String action) {
    }
}
