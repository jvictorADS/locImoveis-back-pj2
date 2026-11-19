package com.locImoveis.pj2back.dto.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoUpdateDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorAcordado,
        String contratoStatus
) {

}
