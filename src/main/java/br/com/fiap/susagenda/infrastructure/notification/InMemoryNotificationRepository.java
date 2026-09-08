package br.com.fiap.susagenda.infrastructure.notification;

import br.com.fiap.susagenda.application.notification.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryNotificationRepository implements NotificationRepository {
    private final Map<String, String> sentNotifications = new ConcurrentHashMap<>();

    @Override
    public boolean wasSent(String appointmentId, LocalDate reminderDate) {
        return sentNotifications.containsKey(key(appointmentId, reminderDate));
    }

    @Override
    public void markSent(String appointmentId, LocalDate reminderDate, String message) {
        sentNotifications.putIfAbsent(key(appointmentId, reminderDate), message);
    }

    private String key(String appointmentId, LocalDate reminderDate) {
        return appointmentId + "|" + reminderDate;
    }
}
