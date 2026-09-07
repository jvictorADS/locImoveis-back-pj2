package com.locImoveis.pj2back.dto.usuario;

import com.locImoveis.pj2back.entity.enums.ContaStatus;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;

public record UsuarioResponseDTO(
    Integer idUsuario,
    String nomeCompleto,
    String email,
    TipoUsuario tipoUsuario,
    ContaStatus contaStatus
) {
}
