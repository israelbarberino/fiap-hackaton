package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.Appointment;
import br.com.fiap.susagenda.domain.appointment.AppointmentScheduleRules;
import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import br.com.fiap.susagenda.domain.appointment.ScheduleRuleException;
import br.com.fiap.susagenda.application.professional.ProfessionalRepository;
import br.com.fiap.susagenda.application.healthunit.HealthUnitRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@ApplicationScoped
public class AppointmentBookingService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    private final AppointmentSlotRepository repository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentHistoryRepository historyRepository;
    private final ProfessionalRepository professionalRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final Clock clock;

    @Inject
    public AppointmentBookingService(AppointmentSlotRepository repository, AppointmentRepository appointmentRepository,
            AppointmentHistoryRepository historyRepository, ProfessionalRepository professionalRepository,
            HealthUnitRepository healthUnitRepository) {
        this(repository, appointmentRepository, historyRepository, professionalRepository, healthUnitRepository,
                Clock.system(BUSINESS_ZONE));
    }

    AppointmentBookingService(AppointmentSlotRepository repository, Clock clock) {
        this(repository, new AppointmentRepository() {
            @Override
            public Appointment save(Appointment appointment) {
                return appointment;
            }

            @Override
            public java.util.Optional<Appointment> findById(String id) {
                return java.util.Optional.empty();
            }

            @Override
            public List<Appointment> findByDate(LocalDate date) {
                return List.of();
            }
        }, new AppointmentHistoryRepository() {
            @Override
            public void append(String appointmentId, Appointment appointment, String action) {
            }

            @Override
            public List<AppointmentHistoryEntry> findByAppointmentId(String appointmentId) {
                return List.of();
            }
        }, id -> java.util.Optional.empty(), id -> java.util.Optional.empty(), clock);
    }

    AppointmentBookingService(AppointmentSlotRepository repository, AppointmentRepository appointmentRepository,
            AppointmentHistoryRepository historyRepository, ProfessionalRepository professionalRepository,
            HealthUnitRepository healthUnitRepository, Clock clock) {
        this.repository = repository;
        this.appointmentRepository = appointmentRepository;
        this.historyRepository = historyRepository;
        this.professionalRepository = professionalRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.clock = clock;
    }

    public Appointment create(String patientId, String professionalId, String unitId, String type,
            AppointmentSlot requestedSlot) {
        book(patientId, professionalId, unitId, type, requestedSlot);
        Appointment appointment = Appointment.create(patientId, professionalId, unitId, type,
                requestedSlot.date(), requestedSlot.startTime());
        appointmentRepository.save(appointment);
        historyRepository.append(appointment.id(), appointment, "CREATED");
        return appointment;
    }

    public AppointmentSlot book(String professionalId, String unitId, AppointmentSlot requestedSlot) {
        return book(null, professionalId, unitId, null, requestedSlot);
    }

    private AppointmentSlot book(String patientId, String professionalId, String unitId, String type,
            AppointmentSlot requestedSlot) {
        validateDependencies(professionalId, unitId);
        LocalDate today = LocalDate.now(clock);
        LocalTime currentTime = LocalTime.now(clock);
        AppointmentScheduleRules.validate(requestedSlot, today, currentTime);

        if (repository.exists(requestedSlot, professionalId)) {
            throw new AppointmentConflictException(
                    "Horário não disponível.",
                    new AppointmentAvailabilityService().suggestAvailableSlots(
                            requestedSlot.date(),
                            requestedSlot.startTime(),
                            repository.findOccupiedSlots(professionalId, unitId)));
        }

        if (!repository.saveIfAvailable(professionalId, unitId, requestedSlot)) {
            throw new AppointmentConflictException(
                "Horário não disponível.",
                new AppointmentAvailabilityService().suggestAvailableSlots(
                    requestedSlot.date(),
                    requestedSlot.startTime(),
                    repository.findOccupiedSlots(professionalId, unitId)));
        }
        return requestedSlot;
    }

    private void validateDependencies(String professionalId, String unitId) {
        var professional = professionalRepository.findById(professionalId)
                .orElseThrow(() -> new InvalidAppointmentReferenceException("Profissional não encontrado."));
        if (!professional.active()) {
            throw new InvalidAppointmentReferenceException("Profissional inativo.");
        }
        var unit = healthUnitRepository.findById(unitId)
                .orElseThrow(() -> new InvalidAppointmentReferenceException("Unidade de saúde não encontrada."));
        if (!unit.active()) {
            throw new InvalidAppointmentReferenceException("Unidade de saúde inativa.");
        }
    }

    public Appointment reschedule(String appointmentId, AppointmentSlot newSlot) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado."));
        AppointmentScheduleRules.validate(newSlot, LocalDate.now(clock), LocalTime.now(clock));
        if (repository.exists(newSlot, appointment.professionalId())) {
            throw new AppointmentConflictException("Horário não disponível.",
                    new AppointmentAvailabilityService().suggestAvailableSlots(newSlot.date(), newSlot.startTime(),
                            repository.findOccupiedSlots(appointment.professionalId(), appointment.unitId())));
        }
        repository.saveIfAvailable(appointment.professionalId(), appointment.unitId(), newSlot);
        Appointment updated = appointment.reschedule(newSlot.date(), newSlot.startTime());
        appointmentRepository.save(updated);
        historyRepository.append(updated.id(), updated, "RESCHEDULED");
        return updated;
    }

    public Appointment cancel(String appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado."));
        Appointment updated = appointment.cancel(reason);
        appointmentRepository.save(updated);
        historyRepository.append(updated.id(), updated, "CANCELLED");
        return updated;
    }

    public Appointment findById(String appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Agendamento não encontrado."));
    }

    public List<AppointmentHistoryRepository.AppointmentHistoryEntry> history(String appointmentId) {
        findById(appointmentId);
        return historyRepository.findByAppointmentId(appointmentId);
    }

    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidAppointmentReferenceException extends RuntimeException {
        public InvalidAppointmentReferenceException(String message) {
            super(message);
        }
    }

    public static class AppointmentConflictException extends RuntimeException {
        private final List<AppointmentSlot> suggestedSlots;

        public AppointmentConflictException(String message, List<AppointmentSlot> suggestedSlots) {
            super(message);
            this.suggestedSlots = suggestedSlots;
        }

        public List<AppointmentSlot> suggestedSlots() {
            return suggestedSlots;
        }
    }
}
