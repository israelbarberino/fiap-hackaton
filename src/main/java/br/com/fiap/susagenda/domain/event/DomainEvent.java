package br.com.fiap.susagenda.domain.event;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent(
        String eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String correlationId,
        String aggregateId,
        Object payload) {

    public static DomainEvent create(String eventType, String aggregateId, Object payload) {
        String correlationId = UUID.randomUUID().toString();
        return new DomainEvent(UUID.randomUUID().toString(), eventType, 1, Instant.now(), correlationId, aggregateId, payload);
    }
}
