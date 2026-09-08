package br.com.fiap.susagenda.infrastructure.professional;

import br.com.fiap.susagenda.application.professional.ProfessionalRepository;
import br.com.fiap.susagenda.domain.professional.Professional;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class DynamoDbProfessionalRepository implements ProfessionalRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbProfessionalRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_PROFESSIONALS_TABLE", defaultValue = "sus-professionals") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public Optional<Professional> findById(String id) {
        var response = client.getItem(GetItemRequest.builder().tableName(tableName)
                .key(Map.of("professionalId", value(id))).consistentRead(true).build());
        if (!response.hasItem()) {
            return Optional.empty();
        }
        var item = response.item();
        return Optional.of(new Professional(
                item.get("professionalId").s(), item.get("name").s(), item.get("cpf").s(),
                item.get("registration").s(), item.get("specialty").s(), item.get("unitId").s(),
                item.get("email").s(), item.get("phone").s(), Boolean.parseBoolean(item.get("active").s())));
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
