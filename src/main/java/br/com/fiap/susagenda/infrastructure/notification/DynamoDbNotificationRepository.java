package br.com.fiap.susagenda.infrastructure.notification;

import br.com.fiap.susagenda.application.notification.NotificationRepository;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.LocalDate;
import java.util.Map;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class DynamoDbNotificationRepository implements NotificationRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbNotificationRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_NOTIFICATIONS_TABLE", defaultValue = "sus-notifications") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public boolean wasSent(String appointmentId, LocalDate reminderDate) {
        return client.getItem(GetItemRequest.builder().tableName(tableName)
                .key(Map.of("notificationId", value(key(appointmentId, reminderDate)))).consistentRead(true).build())
                .hasItem();
    }

    @Override
    public void markSent(String appointmentId, LocalDate reminderDate, String message) {
        try {
            client.putItem(PutItemRequest.builder().tableName(tableName)
                .item(Map.of(
                    "notificationId", value(key(appointmentId, reminderDate)),
                    "appointmentId", value(appointmentId),
                    "reminderDate", value(reminderDate.toString()),
                    "status", value("SENT"),
                    "message", value(message)))
                .conditionExpression("attribute_not_exists(notificationId)")
                .build());
        } catch (ConditionalCheckFailedException ignored) {
            // Idempotent by design for repeated Scheduler executions.
        }
    }

    private String key(String appointmentId, LocalDate reminderDate) {
        return appointmentId + "|" + reminderDate;
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}