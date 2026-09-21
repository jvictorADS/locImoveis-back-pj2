package com.locImoveis.pj2back.dto.usuario;

import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(
        @NotBlank(message = "O nome é obrigatório") String nomeCompleto,
        @NotBlank(message = "O CPF é obrigatório") String cpf,
        @NotBlank(message = "O telefone é obrigatório") String telefone,
        @NotBlank(message = "O email é obrigatório") String email,
        @NotBlank(message = "A senha é obrigatória") String senha,
        @NotNull(message = "O tipo de usuário é obrigatório") TipoUsuario tipoUsuario
) {
}
