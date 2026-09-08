package br.com.fiap.susagenda.infrastructure.notification;

import br.com.fiap.susagenda.application.notification.NotificationSender;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.notification.sender", stringValue = "log")
public class LoggingNotificationSender implements NotificationSender {
    private static final Logger LOG = Logger.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(String recipient, String message) {
        LOG.infof("notification.sent recipient=%s message=%s", recipient, message);
    }
}
