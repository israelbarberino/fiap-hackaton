package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import br.com.fiap.susagenda.domain.healthunit.HealthUnit;
import br.com.fiap.susagenda.domain.professional.Professional;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentBookingServiceTest {
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private final InMemoryTestRepository repository = new InMemoryTestRepository();
    private final AppointmentBookingService service = new AppointmentBookingService(
            repository,
            new AppointmentRepository() {
                @Override
                public br.com.fiap.susagenda.domain.appointment.Appointment save(
                        br.com.fiap.susagenda.domain.appointment.Appointment appointment) {
                    return appointment;
                }

                @Override
                public Optional<br.com.fiap.susagenda.domain.appointment.Appointment> findById(String id) {
                    return Optional.empty();
                }

                @Override
                public List<br.com.fiap.susagenda.domain.appointment.Appointment> findByDate(LocalDate date) {
                    return List.of();
                }
            },
            new AppointmentHistoryRepository() {
                @Override
                public void append(String appointmentId, br.com.fiap.susagenda.domain.appointment.Appointment appointment,
                        String action) {
                }

                @Override
                public List<AppointmentHistoryEntry> findByAppointmentId(String appointmentId) {
                    return List.of();
                }
            },
            id -> Optional.of(new Professional(id, "Dr. Teste", "123", "CRM1", "Clinica Geral", "unit-1",
                "doctor@test.com", "11999999999", true)),
            id -> Optional.of(new HealthUnit(id, "UBS Teste", "Rua Teste", "Centro", "Sao Paulo", "SP", true)),
            Clock.fixed(Instant.parse("2026-09-07T12:00:00Z"), ZONE));

    @Test
    void shouldBookValidSlot() {
        AppointmentSlot slot = new AppointmentSlot(LocalDate.of(2026, 9, 8), LocalTime.of(8, 0));

        assertEquals(slot, service.book("doctor-1", "unit-1", slot));
    }

    @Test
    void shouldReturnConflictWithFiveSuggestions() {
        AppointmentSlot occupied = new AppointmentSlot(LocalDate.of(2026, 9, 8), LocalTime.of(8, 0));
        repository.saveIfAvailable("doctor-1", "unit-1", occupied);

        var exception = assertThrows(AppointmentBookingService.AppointmentConflictException.class,
                () -> service.book("doctor-1", "unit-1", occupied));

        assertEquals("Horário não disponível.", exception.getMessage());
        assertEquals(5, exception.suggestedSlots().size());
    }

    private static final class InMemoryTestRepository implements AppointmentSlotRepository {
        private final Set<AppointmentSlot> slots = new java.util.HashSet<>();

        @Override
        public boolean exists(AppointmentSlot slot, String professionalId) {
            return slots.contains(slot);
        }

        @Override
        public Set<AppointmentSlot> findOccupiedSlots(String professionalId, String unitId) {
            return Set.copyOf(slots);
        }

        @Override
        public boolean saveIfAvailable(String professionalId, String unitId, AppointmentSlot slot) {
            return slots.add(slot);
        }
    }
}
