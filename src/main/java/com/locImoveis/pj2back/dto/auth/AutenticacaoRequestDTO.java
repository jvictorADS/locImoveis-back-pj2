package com.locImoveis.pj2back.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AutenticacaoRequestDTO(
        @NotBlank(message = "O email é obrigatório") String email,
        @NotBlank(message = "A senha é obrigatória") String senha
) {
}
