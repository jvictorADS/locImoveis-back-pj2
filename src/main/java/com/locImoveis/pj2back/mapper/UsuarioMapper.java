package com.locImoveis.pj2back.mapper;

import com.locImoveis.pj2back.dto.usuario.UsuarioRequestDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioResponseDTO;
import com.locImoveis.pj2back.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "contaStatus", ignore = true)
    Usuario toEntity(UsuarioRequestDTO usuarioDTO);

    UsuarioResponseDTO toDTO(Usuario entity);

}
