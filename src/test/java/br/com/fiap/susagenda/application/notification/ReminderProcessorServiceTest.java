package br.com.fiap.susagenda.application.notification;

import br.com.fiap.susagenda.application.appointment.AppointmentRepository;
import br.com.fiap.susagenda.application.event.DomainEventPublisher;
import br.com.fiap.susagenda.application.patient.PatientRepository;
import br.com.fiap.susagenda.application.professional.ProfessionalRepository;
import br.com.fiap.susagenda.application.healthunit.HealthUnitRepository;
import br.com.fiap.susagenda.domain.appointment.Appointment;
import br.com.fiap.susagenda.domain.patient.Patient;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReminderProcessorServiceTest {
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Test
    void shouldSendOnlyOneReminderForEligibleAppointment() {
        LocalDate today = LocalDate.of(2026, 9, 7);
        Appointment appointment = Appointment.create("patient-1", "Dr. Silva", "UBS Centro", "CONSULTA",
                today.plusDays(5), java.time.LocalTime.of(8, 0));
        Patient patient = Patient.create("Maria", "123", "maria@example.com", "11999999999", "Rua A", false);
        InMemoryAppointments appointments = new InMemoryAppointments(appointment);
        InMemoryPatients patients = new InMemoryPatients(patient);
        InMemoryNotifications notifications = new InMemoryNotifications();
        List<String> sentMessages = new ArrayList<>();

        ReminderProcessorService service = new ReminderProcessorService(
                appointments,
                patients,
                notifications,
                (recipient, message) -> sentMessages.add(recipient + ":" + message),
                (eventType, aggregateId, payload) -> {
                },
                id -> Optional.empty(),
                id -> Optional.empty(),
                Clock.fixed(Instant.parse("2026-09-07T12:00:00Z"), ZONE));

        assertEquals(1, service.processDailyReminders());
        assertEquals(0, service.processDailyReminders());
        assertEquals(1, sentMessages.size());
    }

    private record InMemoryAppointments(Appointment appointment) implements AppointmentRepository {
        @Override
        public Appointment save(Appointment value) {
            return value;
        }

        @Override
        public Optional<Appointment> findById(String id) {
            return Optional.of(appointment);
        }

        @Override
        public List<Appointment> findByDate(LocalDate date) {
            return appointment.date().equals(date) ? List.of(appointment) : List.of();
        }
    }

    private record InMemoryPatients(Patient patient) implements PatientRepository {
        @Override
        public Patient save(Patient value) {
            return value;
        }

        @Override
        public Optional<Patient> findById(String id) {
            return Optional.of(patient);
        }

        @Override
        public Optional<Patient> findByCpf(String cpf) {
            return Optional.of(patient);
        }
    }

    private static class InMemoryNotifications implements NotificationRepository {
        private boolean sent;

        @Override
        public boolean wasSent(String appointmentId, LocalDate reminderDate) {
            return sent;
        }

        @Override
        public void markSent(String appointmentId, LocalDate reminderDate, String message) {
            sent = true;
        }
    }
}
