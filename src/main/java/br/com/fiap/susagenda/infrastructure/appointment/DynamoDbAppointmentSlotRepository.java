package br.com.fiap.susagenda.infrastructure.appointment;

import br.com.fiap.susagenda.application.appointment.AppointmentSlotRepository;
import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class DynamoDbAppointmentSlotRepository implements AppointmentSlotRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbAppointmentSlotRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_SLOTS_TABLE", defaultValue = "sus-appointment-slots") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public boolean exists(AppointmentSlot slot, String professionalId) {
        var response = client.query(QueryRequest.builder().tableName(tableName)
            .indexName("professional-slot-index")
            .keyConditionExpression("professionalId = :professionalId")
            .filterExpression("#date = :date AND #time = :time")
            .expressionAttributeNames(Map.of("#date", "date", "#time", "time"))
            .expressionAttributeValues(Map.of(
                ":professionalId", value(professionalId),
                ":date", value(slot.date().toString()),
                ":time", value(slot.startTime().toString())))
            .build());
        return !response.items().isEmpty();
    }

    @Override
    public Set<AppointmentSlot> findOccupiedSlots(String professionalId, String unitId) {
        var response = client.query(QueryRequest.builder().tableName(tableName)
            .indexName("professional-slot-index")
            .keyConditionExpression("professionalId = :professionalId")
            .filterExpression("unitId = :unitId")
            .expressionAttributeValues(Map.of(
                ":professionalId", value(professionalId),
                ":unitId", value(unitId)))
            .build());
        return response.items().stream()
            .map(item -> new AppointmentSlot(
                LocalDate.parse(item.get("date").s()),
                LocalTime.parse(item.get("time").s())))
            .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public boolean saveIfAvailable(String professionalId, String unitId, AppointmentSlot slot) {
        try {
            client.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(Map.of(
                            "slotId", value(slotId(professionalId, unitId, slot)),
                            "slotKey", value(slot.date() + "|" + slot.startTime()),
                            "professionalId", value(professionalId),
                            "unitId", value(unitId),
                            "date", value(slot.date().toString()),
                            "time", value(slot.startTime().toString())))
                    .conditionExpression("attribute_not_exists(slotId)")
                    .build());
            return true;
        } catch (ConditionalCheckFailedException exception) {
            return false;
        }
    }

    private String slotId(String professionalId, String unitId, AppointmentSlot slot) {
        return professionalId + "|" + unitId + "|" + slot.date() + "|" + slot.startTime();
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}