package com.locImoveis.pj2back.mapper;

import com.locImoveis.pj2back.dto.cobranca.CobrancaRequestDTO;
import com.locImoveis.pj2back.dto.cobranca.CobrancaResponseDTO;
import com.locImoveis.pj2back.entity.Cobranca;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CobrancaMapper {

    @Mapping(target = "idCobranca", ignore = true)
    @Mapping(target = "contrato", ignore = true)
    @Mapping(target = "cobrancaStatus", ignore = true)
    @Mapping(target = "dataPagamento", ignore = true)
    Cobranca toEntity(CobrancaRequestDTO dto);

    CobrancaResponseDTO toDto(Cobranca entity);
}
