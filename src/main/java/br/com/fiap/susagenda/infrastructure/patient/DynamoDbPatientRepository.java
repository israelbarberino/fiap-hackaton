package br.com.fiap.susagenda.infrastructure.patient;

import br.com.fiap.susagenda.application.patient.PatientRepository;
import br.com.fiap.susagenda.domain.patient.Patient;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class DynamoDbPatientRepository implements PatientRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbPatientRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_PATIENTS_TABLE", defaultValue = "sus-patients") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public Patient save(Patient patient) {
        client.putItem(PutItemRequest.builder().tableName(tableName).item(Map.of(
                "patientId", value(patient.id()),
                "cpf", value(patient.cpf()),
                "name", value(patient.name()),
                "email", value(patient.email()),
                "phone", value(patient.phone() == null ? "" : patient.phone()),
                "address", value(patient.address() == null ? "" : patient.address()),
                "whatsappEnabled", value(Boolean.toString(patient.whatsappEnabled())),
                "createdAt", value(patient.createdAt().toString()),
                "updatedAt", value(patient.updatedAt().toString()))).build());
        return patient;
    }

    @Override
    public Optional<Patient> findById(String id) {
        var response = client.getItem(GetItemRequest.builder().tableName(tableName)
                .key(Map.of("patientId", value(id))).consistentRead(true).build());
        return response.hasItem() ? Optional.of(fromItem(response.item())) : Optional.empty();
    }

    @Override
    public Optional<Patient> findByCpf(String cpf) {
        var response = client.query(QueryRequest.builder().tableName(tableName).indexName("cpf-index")
                .keyConditionExpression("cpf = :cpf")
                .expressionAttributeValues(Map.of(":cpf", value(cpf))).limit(1).build());
        return response.items().stream().findFirst().map(this::fromItem);
    }

    private Patient fromItem(Map<String, AttributeValue> item) {
        return new Patient(item.get("patientId").s(), item.get("name").s(), item.get("cpf").s(),
                item.get("email").s(), item.get("phone").s(), item.get("address").s(),
                Boolean.parseBoolean(item.get("whatsappEnabled").s()), Instant.parse(item.get("createdAt").s()),
                Instant.parse(item.get("updatedAt").s()));
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
