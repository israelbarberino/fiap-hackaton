package br.com.fiap.susagenda.domain.healthunit;

public record HealthUnit(String id, String name, String address, String neighborhood, String city, String state,
        boolean active) {
}
