package com.locImoveis.pj2back.dto.cobranca;

import com.locImoveis.pj2back.entity.enums.CobrancaStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobrancaResponseDTO(
        Integer idCobranca,
        String mesReferencia,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        BigDecimal valor,
        CobrancaStatus cobrancaStatus
) {
}
