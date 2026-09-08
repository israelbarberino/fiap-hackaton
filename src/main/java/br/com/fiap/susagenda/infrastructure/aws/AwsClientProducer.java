package br.com.fiap.susagenda.infrastructure.aws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class AwsClientProducer {
    @Produces
    @ApplicationScoped
    public DynamoDbClient dynamoDbClient(
            @ConfigProperty(name = "AWS_REGION", defaultValue = "us-east-1") String region,
            @ConfigProperty(name = "DYNAMODB_ENDPOINT") Optional<String> endpoint) {
        var builder = DynamoDbClient.builder().region(Region.of(region));
        endpoint.filter(value -> !value.isBlank()).ifPresent(value -> builder.endpointOverride(URI.create(value)));
        return builder.build();
    }

    @Produces
    @ApplicationScoped
    public SesClient sesClient(
            @ConfigProperty(name = "AWS_REGION", defaultValue = "us-east-1") String region,
            @ConfigProperty(name = "SES_ENDPOINT") Optional<String> endpoint) {
        var builder = SesClient.builder().region(Region.of(region));
        endpoint.filter(value -> !value.isBlank()).ifPresent(value -> builder.endpointOverride(URI.create(value)));
        return builder.build();
    }

    @Produces
    @ApplicationScoped
    public EventBridgeClient eventBridgeClient(
            @ConfigProperty(name = "AWS_REGION", defaultValue = "us-east-1") String region) {
        return EventBridgeClient.builder().region(Region.of(region)).build();
    }
}