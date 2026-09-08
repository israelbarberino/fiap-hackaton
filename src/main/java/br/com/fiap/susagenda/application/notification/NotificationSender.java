package br.com.fiap.susagenda.application.notification;

public interface NotificationSender {
    void send(String recipient, String message);
}
