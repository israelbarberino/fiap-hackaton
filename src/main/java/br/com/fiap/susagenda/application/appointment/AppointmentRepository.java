package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.Appointment;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);

    Optional<Appointment> findById(String id);

    List<Appointment> findByDate(LocalDate date);
}
