package com.locImoveis.pj2back.dto.manutencao;

import com.locImoveis.pj2back.entity.enums.PedidoStatus;
import jakarta.validation.constraints.NotNull;

public record ManutencaoStatusRequestDTO(@NotNull PedidoStatus status) {
}
