package com.locImoveis.pj2back.dto.contrato;

import com.locImoveis.pj2back.entity.enums.ContratoStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoResponseDTO(
        Integer idContrato,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorAcordado,
        LocalDate dataProximoReajuste,
        ContratoStatus contratoStatus
) {
}
