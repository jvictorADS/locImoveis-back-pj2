package com.locImoveis.pj2back.dto.imovel;

import java.math.BigDecimal;

public record ImovelUpdateDTO(
        BigDecimal valorAluguel,
        BigDecimal valorIPTU
) {
}
