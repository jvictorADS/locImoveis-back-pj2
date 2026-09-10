package com.locImoveis.pj2back.mapper;

import com.locImoveis.pj2back.dto.contrato.ContratoRequestDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoResponseDTO;
import com.locImoveis.pj2back.entity.Contrato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContratoMapper {

    @Mapping(target = "idContrato", ignore = true)
    @Mapping(target = "imovel", ignore = true)
    @Mapping(target = "locatario", ignore = true)
    @Mapping(target = "contratoStatus", ignore = true)
    @Mapping(target = "dataProximoReajuste", ignore = true)
    Contrato toEntity(ContratoRequestDTO dto);

    ContratoResponseDTO toDto(Contrato entity);

}
