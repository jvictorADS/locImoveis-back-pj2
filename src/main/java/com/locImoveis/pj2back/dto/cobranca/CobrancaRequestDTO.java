package com.locImoveis.pj2back.dto.cobranca;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobrancaRequestDTO(
        Integer contratoId,
        String mesReferencia,
        LocalDate dataVencimento,
        BigDecimal valor
) {
}
