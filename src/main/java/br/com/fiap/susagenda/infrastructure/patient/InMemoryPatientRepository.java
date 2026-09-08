package br.com.fiap.susagenda.infrastructure.patient;

import br.com.fiap.susagenda.application.patient.PatientRepository;
import br.com.fiap.susagenda.domain.patient.Patient;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryPatientRepository implements PatientRepository {
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();

    @Override
    public Patient save(Patient patient) {
        patients.put(patient.id(), patient);
        return patient;
    }

    @Override
    public Optional<Patient> findById(String id) {
        return Optional.ofNullable(patients.get(id));
    }

    @Override
    public Optional<Patient> findByCpf(String cpf) {
        return patients.values().stream().filter(patient -> patient.cpf().equals(cpf)).findFirst();
    }
}
