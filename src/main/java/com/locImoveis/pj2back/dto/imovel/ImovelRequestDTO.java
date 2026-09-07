package com.locImoveis.pj2back.dto.imovel;

import java.math.BigDecimal;

public record ImovelRequestDTO(
    String endereco,
    BigDecimal valorAluguel,
    BigDecimal valorIPTU,
    Integer locadorId
) {
}
