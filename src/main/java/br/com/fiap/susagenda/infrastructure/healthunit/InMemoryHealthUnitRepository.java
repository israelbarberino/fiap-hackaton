package br.com.fiap.susagenda.infrastructure.healthunit;

import br.com.fiap.susagenda.application.healthunit.HealthUnitRepository;
import br.com.fiap.susagenda.domain.healthunit.HealthUnit;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryHealthUnitRepository implements HealthUnitRepository {
    private final Map<String, HealthUnit> units = new ConcurrentHashMap<>();

    public InMemoryHealthUnitRepository() {
        save(new HealthUnit("unit-api-1", "UBS Centro 1", "Rua A, 100", "Centro", "Sao Paulo", "SP", true));
        save(new HealthUnit("unit-api-2", "UBS Centro 2", "Rua B, 200", "Centro", "Sao Paulo", "SP", true));
        save(new HealthUnit("unit-e2e", "UBS Centro", "Rua Principal, 100", "Centro", "Sao Paulo", "SP", true));
    }

    public void save(HealthUnit unit) {
        units.put(unit.id(), unit);
    }

    @Override
    public Optional<HealthUnit> findById(String id) {
        return Optional.ofNullable(units.get(id));
    }
}
