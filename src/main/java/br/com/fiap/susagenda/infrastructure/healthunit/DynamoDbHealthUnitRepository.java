package br.com.fiap.susagenda.infrastructure.healthunit;

import br.com.fiap.susagenda.application.healthunit.HealthUnitRepository;
import br.com.fiap.susagenda.domain.healthunit.HealthUnit;
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
public class DynamoDbHealthUnitRepository implements HealthUnitRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbHealthUnitRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_HEALTH_UNITS_TABLE", defaultValue = "sus-health-units") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public Optional<HealthUnit> findById(String id) {
        var response = client.getItem(GetItemRequest.builder().tableName(tableName)
                .key(Map.of("unitId", value(id))).consistentRead(true).build());
        if (!response.hasItem()) {
            return Optional.empty();
        }
        var item = response.item();
        return Optional.of(new HealthUnit(
                item.get("unitId").s(), item.get("name").s(), item.get("address").s(),
                item.get("neighborhood").s(), item.get("city").s(), item.get("state").s(),
                Boolean.parseBoolean(item.get("active").s())));
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
