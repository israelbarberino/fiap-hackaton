package br.com.fiap.susagenda.application.notification;

import br.com.fiap.susagenda.application.appointment.AppointmentRepository;
import br.com.fiap.susagenda.application.event.DomainEventPublisher;
import br.com.fiap.susagenda.application.patient.PatientRepository;
import br.com.fiap.susagenda.application.professional.ProfessionalRepository;
import br.com.fiap.susagenda.application.healthunit.HealthUnitRepository;
import br.com.fiap.susagenda.domain.appointment.Appointment;
import br.com.fiap.susagenda.domain.appointment.AppointmentStatus;
import br.com.fiap.susagenda.domain.patient.Patient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ReminderProcessorService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    @Inject
    public ReminderProcessorService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
            NotificationRepository notificationRepository, NotificationSender notificationSender,
            DomainEventPublisher eventPublisher, ProfessionalRepository professionalRepository,
            HealthUnitRepository healthUnitRepository) {
        this(appointmentRepository, patientRepository, notificationRepository, notificationSender, eventPublisher,
                professionalRepository, healthUnitRepository, Clock.system(BUSINESS_ZONE));
    }

    ReminderProcessorService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
            NotificationRepository notificationRepository, NotificationSender notificationSender,
            DomainEventPublisher eventPublisher, ProfessionalRepository professionalRepository,
            HealthUnitRepository healthUnitRepository, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.professionalRepository = professionalRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public int processDailyReminders() {
        LocalDate reminderDate = LocalDate.now(clock).plusDays(5);
        AtomicInteger sent = new AtomicInteger();
        appointmentRepository.findByDate(reminderDate).stream()
                .filter(this::isEligible)
                .forEach(appointment -> {
                    if (notificationRepository.wasSent(appointment.id(), reminderDate)) {
                        return;
                    }
                    Patient patient = patientRepository.findById(appointment.patientId()).orElse(null);
                    if (patient == null) {
                        return;
                    }
                    String message = messageFor(appointment, patient);
                    notificationSender.send(patient.email(), message);
                    notificationRepository.markSent(appointment.id(), reminderDate, message);
                    eventPublisher.publish("ReminderSent", appointment.id(), appointment);
                    sent.incrementAndGet();
                });
        return sent.get();
    }

    private boolean isEligible(Appointment appointment) {
        return appointment.status() == AppointmentStatus.AGENDADO
                || appointment.status() == AppointmentStatus.CONFIRMADO
                || appointment.status() == AppointmentStatus.REAGENDADO;
    }

    private String messageFor(Appointment appointment, Patient patient) {
        String professionalName = professionalRepository.findById(appointment.professionalId())
            .map(professional -> professional.name()).orElse(appointment.professionalId());
        String unitName = healthUnitRepository.findById(appointment.unitId())
            .map(unit -> unit.name() + ", " + unit.neighborhood() + ", " + unit.city())
            .orElse(appointment.unitId());
        return "Olá, " + patient.name() + "! Você tem um agendamento de " + appointment.type()
                + ", para o dia " + appointment.date().format(DATE_FORMATTER) + " às "
            + appointment.time().format(TIME_FORMATTER) + " com o Dr(a) " + professionalName
            + " na unidade " + unitName
                + ". Se quiser reagendar, acesse {linkReagendamento}.";
    }
}
