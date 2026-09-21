package com.locImoveis.pj2back.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record UsuarioUpdateDTO(
        @NotBlank(message = "O nome é obrigatório") String nomeCompleto,
        @NotBlank(message = "O telefone é obrigatório") String telefone
) {
}
