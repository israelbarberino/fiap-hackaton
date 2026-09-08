package br.com.fiap.susagenda.infrastructure.event;

import br.com.fiap.susagenda.application.event.DomainEventPublisher;
import br.com.fiap.susagenda.domain.event.DomainEvent;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryDomainEventPublisher implements DomainEventPublisher {
    private final List<DomainEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void publish(String eventType, String aggregateId, Object payload) {
        events.add(DomainEvent.create(eventType, aggregateId, payload));
    }

    public List<DomainEvent> events() {
        return List.copyOf(events);
    }
}
