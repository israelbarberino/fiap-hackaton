package br.com.fiap.susagenda.infrastructure.user;

import br.com.fiap.susagenda.application.user.UserRepository;
import br.com.fiap.susagenda.domain.user.User;
import br.com.fiap.susagenda.domain.user.UserRole;
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
public class DynamoDbUserRepository implements UserRepository {
    private final DynamoDbClient client;
    private final String tableName;

    @Inject
    public DynamoDbUserRepository(DynamoDbClient client,
            @ConfigProperty(name = "DYNAMODB_USERS_TABLE", defaultValue = "sus-users") String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    @Override
    public User save(User user) {
        client.putItem(PutItemRequest.builder().tableName(tableName).item(Map.of(
                "userId", value(user.id()),
                "cpf", value(user.cpf()),
                "email", value(user.email().toLowerCase()),
                "name", value(user.name()),
                "passwordHash", value(user.passwordHash()),
                "role", value(user.role().name()),
                "active", value(Boolean.toString(user.active())),
                "createdAt", value(user.createdAt().toString()))).build());
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        var response = client.getItem(GetItemRequest.builder().tableName(tableName)
                .key(Map.of("userId", value(id))).consistentRead(true).build());
        return response.hasItem() ? Optional.of(fromItem(response.item())) : Optional.empty();
    }

    @Override
    public Optional<User> findByLogin(String login) {
        String index = login.contains("@") ? "email-index" : "cpf-index";
        String attribute = login.contains("@") ? "email" : "cpf";
        var response = client.query(QueryRequest.builder().tableName(tableName).indexName(index)
                .keyConditionExpression(attribute + " = :value")
                .expressionAttributeValues(Map.of(":value", value(login.toLowerCase())))
                .limit(1).build());
        return response.items().stream().findFirst().map(this::fromItem);
    }

    private User fromItem(Map<String, AttributeValue> item) {
        return new User(item.get("userId").s(), item.get("name").s(), item.get("cpf").s(), item.get("email").s(),
                item.get("passwordHash").s(), UserRole.valueOf(item.get("role").s()),
                Boolean.parseBoolean(item.get("active").s()), Instant.parse(item.get("createdAt").s()));
    }

    private AttributeValue value(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
