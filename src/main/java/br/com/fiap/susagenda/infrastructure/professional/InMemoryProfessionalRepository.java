package br.com.fiap.susagenda.infrastructure.professional;

import br.com.fiap.susagenda.application.professional.ProfessionalRepository;
import br.com.fiap.susagenda.domain.professional.Professional;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryProfessionalRepository implements ProfessionalRepository {
    private final Map<String, Professional> professionals = new ConcurrentHashMap<>();

    public InMemoryProfessionalRepository() {
    save(new Professional("doctor-api-1", "Dra. Ana Costa", "11111111111", "CRM1111", "Clinica Geral",
        "unit-api-1", "ana@susmvp.com.br", "11911111111", true));
    save(new Professional("doctor-api-2", "Dr. Bruno Lima", "22222222222", "CRM2222", "Clinica Geral",
        "unit-api-2", "bruno@susmvp.com.br", "11922222222", true));
    save(new Professional("doctor-e2e", "Dra. Marina Souza", "33333333333", "CRM3333", "Clinica Geral",
        "unit-e2e", "marina@susmvp.com.br", "11933333333", true));
    }

    public void save(Professional professional) {
        professionals.put(professional.id(), professional);
    }

    @Override
    public Optional<Professional> findById(String id) {
        return Optional.ofNullable(professionals.get(id));
    }
}
