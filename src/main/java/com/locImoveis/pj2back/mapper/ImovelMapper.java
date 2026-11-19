package com.locImoveis.pj2back.mapper;

import com.locImoveis.pj2back.dto.imovel.ImovelRequestDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelResponseDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelUpdateDTO;
import com.locImoveis.pj2back.entity.Imovel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImovelMapper {

    @Mapping(target = "idImovel", ignore = true)
    @Mapping(target = "ocupacaoStatus", ignore = true)
    @Mapping(target = "locador", ignore = true)
    Imovel toEntity(ImovelRequestDTO dto);

    ImovelResponseDTO toDto(Imovel entity);

    // pega dados do dto e joga para dentro da entidade sem apagar campos
    void updateEntityFromDto(ImovelUpdateDTO dto, @MappingTarget Imovel entity);

}
