package br.com.fiap.susagenda.application.patient;

import br.com.fiap.susagenda.domain.patient.Patient;

import java.util.Optional;

public interface PatientRepository {
    Patient save(Patient patient);

    Optional<Patient> findById(String id);

    Optional<Patient> findByCpf(String cpf);
}
