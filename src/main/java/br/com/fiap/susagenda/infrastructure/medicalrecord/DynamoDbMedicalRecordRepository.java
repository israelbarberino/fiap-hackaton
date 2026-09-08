package br.com.fiap.susagenda.infrastructure.medicalrecord;

import br.com.fiap.susagenda.application.medicalrecord.MedicalRecordRepository;
import br.com.fiap.susagenda.domain.medicalrecord.MedicalRecordEntry;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class DynamoDbMedicalRecordRepository implements MedicalRecordRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbMedicalRecordRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_MEDICAL_RECORDS_TABLE", defaultValue = "sus-medical-records") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public MedicalRecordEntry append(MedicalRecordEntry entry) {
        client.putItem(PutItemRequest.builder().tableName(tableName).item(Map.of(
                "entryId", value(entry.entryId()),
                "patientId", value(entry.patientId()),
                "createdAt", value(entry.createdAt().toString()),
                "professionalId", value(entry.professionalId()),
                "appointmentId", value(entry.appointmentId() == null ? "" : entry.appointmentId()),
                "evolutionType", value(entry.evolutionType() == null ? "" : entry.evolutionType()),
                "description", value(entry.description()),
                "observations", value(entry.observations() == null ? "" : entry.observations()))).build());
        return entry;
    }

    @Override
    public List<MedicalRecordEntry> findByPatientId(String patientId) {
        return client.query(QueryRequest.builder().tableName(tableName).indexName("patient-created-index")
                .keyConditionExpression("patientId = :patientId")
                .expressionAttributeValues(Map.of(":patientId", value(patientId)))
                .scanIndexForward(true).build()).items().stream().map(item -> new MedicalRecordEntry(
                        item.get("entryId").s(), item.get("patientId").s(), item.get("professionalId").s(),
                        item.get("appointmentId").s(), item.get("evolutionType").s(), item.get("description").s(),
                        item.get("observations").s(), Instant.parse(item.get("createdAt").s()))).toList();
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
