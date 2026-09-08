package br.com.fiap.susagenda.infrastructure.appointment;

import br.com.fiap.susagenda.application.appointment.AppointmentSlotRepository;
import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryAppointmentSlotRepository implements AppointmentSlotRepository {
    private final Map<String, Set<AppointmentSlot>> slotsByProfessionalAndUnit = new ConcurrentHashMap<>();

    @Override
    public boolean exists(AppointmentSlot slot, String professionalId) {
        return slotsByProfessionalAndUnit.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(professionalId + "|"))
                .anyMatch(entry -> entry.getValue().contains(slot));
    }

    @Override
    public Set<AppointmentSlot> findOccupiedSlots(String professionalId, String unitId) {
        return Set.copyOf(slotsByProfessionalAndUnit.getOrDefault(key(professionalId, unitId), Set.of()));
    }

    @Override
    public synchronized boolean saveIfAvailable(String professionalId, String unitId, AppointmentSlot slot) {
        if (exists(slot, professionalId)) {
            return false;
        }
        slotsByProfessionalAndUnit
                .computeIfAbsent(key(professionalId, unitId), ignored -> ConcurrentHashMap.newKeySet())
                .add(slot);
        return true;
    }

    private String key(String professionalId, String unitId) {
        return professionalId + "|" + unitId;
    }
}
