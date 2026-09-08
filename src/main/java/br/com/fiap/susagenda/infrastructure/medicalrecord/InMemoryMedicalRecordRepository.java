package br.com.fiap.susagenda.infrastructure.medicalrecord;

import br.com.fiap.susagenda.application.medicalrecord.MedicalRecordRepository;
import br.com.fiap.susagenda.domain.medicalrecord.MedicalRecordEntry;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.properties.IfBuildProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@IfBuildProperty(name = "sus-agenda.persistence", stringValue = "memory")
public class InMemoryMedicalRecordRepository implements MedicalRecordRepository {
    private final Map<String, List<MedicalRecordEntry>> entriesByPatient = new ConcurrentHashMap<>();

    @Override
    public MedicalRecordEntry append(MedicalRecordEntry entry) {
        entriesByPatient.computeIfAbsent(entry.patientId(), ignored -> new ArrayList<>()).add(entry);
        return entry;
    }

    @Override
    public List<MedicalRecordEntry> findByPatientId(String patientId) {
        return List.copyOf(entriesByPatient.getOrDefault(patientId, List.of()));
    }
}
