package com.locImoveis.pj2back.dto.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoRequestDTO(
        Integer imovelId,
        Integer locatarioId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer diaVencimento,
        BigDecimal valorAcordado,
        String indiceReajuste
) {
}
