package com.locImoveis.pj2back.dto.manutencao;

import com.locImoveis.pj2back.entity.enums.Prioridade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ManutencaoResponseDTO(
        Integer idPedido,
        LocalDate dataAbertura,
        String descricaoProblema,
        BigDecimal valorGasto,
        Prioridade prioridade,
        Integer contratoId,
        String locatarioNome,
        String imovelEndereco
) {
}