package com.locImoveis.pj2back.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDTO(
        BigDecimal recebidoMes,
        BigDecimal gastosReformas,
        BigDecimal totalPendente,
        long imoveisAlugados,
        long imoveisVagos,
        List<InadimplenteDTO> inadimplentes,
        List<FluxoCaixaMensalDTO> fluxoCaixa
) {
    public record FluxoCaixaMensalDTO(
            String mes,
            BigDecimal recebido,
            BigDecimal gastosReformas,
            BigDecimal inadimplencia
    ) {}

    public record InadimplenteDTO(
            Integer id,
            String inquilino,
            String imovel,
            long diasAtraso,
            BigDecimal valor
    ) {}
}