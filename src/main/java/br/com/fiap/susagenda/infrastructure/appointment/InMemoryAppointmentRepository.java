package br.com.fiap.susagenda.infrastructure.appointment;

import br.com.fiap.susagenda.application.appointment.AppointmentRepository;
import br.com.fiap.susagenda.domain.appointment.Appointment;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;

import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<String, Appointment> appointments = new ConcurrentHashMap<>();

    @Override
    public Appointment save(Appointment appointment) {
        appointments.put(appointment.id(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(String id) {
        return Optional.ofNullable(appointments.get(id));
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return appointments.values().stream()
                .filter(appointment -> appointment.date().equals(date))
                .toList();
    }
}
