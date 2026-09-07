package com.locImoveis.pj2back.dto.usuario;

import com.locImoveis.pj2back.entity.enums.TipoUsuario;

public record UsuarioRequestDTO(
        String nomeCompleto,
        String cpf,
        String telefone,
        String email,
        String senha,
        TipoUsuario tipoUsuario
) {
}
