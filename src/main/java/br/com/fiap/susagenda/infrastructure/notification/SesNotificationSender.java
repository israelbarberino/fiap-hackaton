package br.com.fiap.susagenda.infrastructure.notification;

import br.com.fiap.susagenda.application.notification.NotificationSender;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.notification.sender", stringValue = "ses")
public class SesNotificationSender implements NotificationSender {
    private final SesClient client;
    private final String sender;

    @Inject
    public SesNotificationSender(SesClient client,
            @ConfigProperty(name = "SES_FROM_EMAIL") String sender) {
        this.client = client;
        this.sender = sender;
    }

    @Override
    public void send(String recipient, String message) {
        client.sendEmail(SendEmailRequest.builder()
                .source(sender)
                .destination(Destination.builder().toAddresses(recipient).build())
                .message(Message.builder()
                        .subject(Content.builder().data("Lembrete de agendamento - SUS Agenda+").build())
                        .body(Body.builder().text(Content.builder().data(message).build()).build())
                        .build())
                .build());
    }
}