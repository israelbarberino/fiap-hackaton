package br.com.fiap.susagenda.application.medicalrecord;

import br.com.fiap.susagenda.domain.medicalrecord.MedicalRecordEntry;

import java.util.List;

public interface MedicalRecordRepository {
    MedicalRecordEntry append(MedicalRecordEntry entry);

    List<MedicalRecordEntry> findByPatientId(String patientId);
}
