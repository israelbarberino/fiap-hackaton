package br.com.fiap.susagenda.infrastructure.appointment;

import br.com.fiap.susagenda.application.appointment.AppointmentHistoryRepository;
import br.com.fiap.susagenda.domain.appointment.Appointment;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryAppointmentHistoryRepository implements AppointmentHistoryRepository {
    private final Map<String, List<AppointmentHistoryEntry>> history = new ConcurrentHashMap<>();

    @Override
    public void append(String appointmentId, Appointment appointment, String action) {
        history.computeIfAbsent(appointmentId, ignored -> new ArrayList<>())
                .add(new AppointmentHistoryEntry(appointmentId, appointment, action));
    }

    @Override
    public List<AppointmentHistoryEntry> findByAppointmentId(String appointmentId) {
        return List.copyOf(history.getOrDefault(appointmentId, List.of()));
    }
}
