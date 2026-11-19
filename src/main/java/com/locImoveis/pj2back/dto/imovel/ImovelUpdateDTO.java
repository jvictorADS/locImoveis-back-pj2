package com.locImoveis.pj2back.dto.imovel;

import java.math.BigDecimal;

public record ImovelUpdateDTO(
        String endereco,
        BigDecimal valorAluguel,
        BigDecimal valorIPTU
) {
}
