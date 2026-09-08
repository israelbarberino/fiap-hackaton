package br.com.fiap.susagenda.infrastructure.event;

import br.com.fiap.susagenda.application.event.DomainEventPublisher;
import br.com.fiap.susagenda.domain.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "dynamodb")
public class EventBridgeDomainEventPublisher implements DomainEventPublisher {
    private final EventBridgeClient client;
    private final ObjectMapper mapper;
    private final String busName;

    @Inject
    public EventBridgeDomainEventPublisher(EventBridgeClient client, ObjectMapper mapper,
            @ConfigProperty(name = "EVENT_BUS_NAME", defaultValue = "default") String busName) {
        this.client = client;
        this.mapper = mapper;
        this.busName = busName;
    }

    @Override
    public void publish(String eventType, String aggregateId, Object payload) {
        try {
            DomainEvent event = DomainEvent.create(eventType, aggregateId, payload);
            client.putEvents(PutEventsRequest.builder().entries(PutEventsRequestEntry.builder()
                    .eventBusName(busName)
                    .source("br.com.fiap.susagenda")
                    .detailType(eventType)
                    .detail(mapper.writeValueAsString(event))
                    .build()).build());
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível publicar o evento de domínio.", exception);
        }
    }
}
