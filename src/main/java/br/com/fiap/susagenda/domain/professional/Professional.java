package br.com.fiap.susagenda.domain.professional;

public record Professional(String id, String name, String cpf, String registration, String specialty, String unitId,
        String email, String phone, boolean active) {
}
