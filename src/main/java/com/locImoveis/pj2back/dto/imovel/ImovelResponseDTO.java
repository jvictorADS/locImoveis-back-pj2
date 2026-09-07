package com.locImoveis.pj2back.dto.imovel;

import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;

import java.math.BigDecimal;

public record ImovelResponseDTO(
        Integer idImovel,
        String endereco,
        BigDecimal valorAluguel,
        OcupacaoStatus ocupacaoStatus
) {
}
