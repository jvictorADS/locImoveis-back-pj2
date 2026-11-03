package com.locImoveis.pj2back.dto.manutencao;

import com.locImoveis.pj2back.entity.enums.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ManutencaoRequestDTO(
        @NotNull @Positive Integer contratoId,
        @NotBlank String descricaoProblema,
        @NotNull Prioridade prioridade,
        @PositiveOrZero BigDecimal valorGasto
) {
}