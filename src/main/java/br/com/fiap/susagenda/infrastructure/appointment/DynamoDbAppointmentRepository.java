package br.com.fiap.susagenda.infrastructure.appointment;

import br.com.fiap.susagenda.application.appointment.AppointmentRepository;
import br.com.fiap.susagenda.domain.appointment.Appointment;
import br.com.fiap.susagenda.domain.appointment.AppointmentStatus;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class DynamoDbAppointmentRepository implements AppointmentRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbAppointmentRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_APPOINTMENTS_TABLE", defaultValue = "sus-appointments") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public Appointment save(Appointment appointment) {
        client.putItem(PutItemRequest.builder().tableName(tableName).item(toItem(appointment)).build());
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(String id) {
        var response = client.getItem(GetItemRequest.builder().tableName(tableName)
                .key(Map.of("appointmentId", value(id))).consistentRead(true).build());
        return response.hasItem() ? Optional.of(fromItem(response.item())) : Optional.empty();
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        var response = client.query(QueryRequest.builder().tableName(tableName)
                .indexName("appointment-date-index")
                .keyConditionExpression("appointmentDate = :date")
                .expressionAttributeValues(Map.of(":date", value(date.toString())))
                .build());
        return response.items().stream().map(this::fromItem).toList();
    }

    private Map<String, AttributeValue> toItem(Appointment appointment) {
        return Map.of(
                "appointmentId", value(appointment.id()),
                "appointmentDate", value(appointment.date().toString()),
                "patientId", value(appointment.patientId()),
                "professionalId", value(appointment.professionalId()),
                "unitId", value(appointment.unitId()),
                "type", value(appointment.type()),
                "time", value(appointment.time().toString()),
                "status", value(appointment.status().name()),
                "createdAt", value(appointment.createdAt().toString()),
                "updatedAt", value(appointment.updatedAt().toString()));
    }

    private Appointment fromItem(Map<String, AttributeValue> item) {
        return new Appointment(
                item.get("appointmentId").s(),
                item.get("patientId").s(),
                item.get("professionalId").s(),
                item.get("unitId").s(),
                item.get("type").s(),
                LocalDate.parse(item.get("appointmentDate").s()),
                LocalTime.parse(item.get("time").s()),
                AppointmentStatus.valueOf(item.get("status").s()),
                item.containsKey("cancellationReason") ? item.get("cancellationReason").s() : null,
                Instant.parse(item.get("createdAt").s()),
                Instant.parse(item.get("updatedAt").s()));
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}