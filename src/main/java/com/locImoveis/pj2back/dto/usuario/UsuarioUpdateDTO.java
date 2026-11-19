package com.locImoveis.pj2back.dto.usuario;

import com.locImoveis.pj2back.entity.enums.ContaStatus;
import jakarta.validation.constraints.NotBlank;

public record UsuarioUpdateDTO(
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        ContaStatus contaStatus

) {
}
