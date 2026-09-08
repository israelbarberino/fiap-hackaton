package br.com.fiap.susagenda.application.professional;

import br.com.fiap.susagenda.domain.professional.Professional;

import java.util.Optional;

public interface ProfessionalRepository {
    Optional<Professional> findById(String id);
}
