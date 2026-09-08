package br.com.fiap.susagenda.application.healthunit;

import br.com.fiap.susagenda.domain.healthunit.HealthUnit;

import java.util.Optional;

public interface HealthUnitRepository {
    Optional<HealthUnit> findById(String id);
}
