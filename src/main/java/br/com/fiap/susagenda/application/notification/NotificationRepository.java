package br.com.fiap.susagenda.application.notification;

import java.time.LocalDate;

public interface NotificationRepository {
    boolean wasSent(String appointmentId, LocalDate reminderDate);

    void markSent(String appointmentId, LocalDate reminderDate, String message);
}
