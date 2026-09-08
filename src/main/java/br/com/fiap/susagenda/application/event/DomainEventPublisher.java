package br.com.fiap.susagenda.application.event;

public interface DomainEventPublisher {
    void publish(String eventType, String aggregateId, Object payload);
}
