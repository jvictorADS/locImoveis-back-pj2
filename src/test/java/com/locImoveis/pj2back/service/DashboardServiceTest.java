package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.PedidoManutencao;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.repository.CobrancaRepository;
import com.locImoveis.pj2back.repository.ImovelRepository;
import com.locImoveis.pj2back.repository.PedidoManutencaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private ImovelRepository imovelRepository;

    @Mock
    private PedidoManutencaoRepository manutencaoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void somaSomentePagamentosConfirmadosNoMesAtual() {
        Integer locadorId = 12;
        LocalDate hoje = LocalDate.now();
        List<Cobranca> cobrancas = List.of(
                Cobranca.builder()
                        .valor(new BigDecimal("1250.00"))
                        .cobrancaStatus(CobrancaStatus.PAGA)
                        .dataPagamento(hoje)
                        .build(),
                Cobranca.builder()
                        .valor(new BigDecimal("900.00"))
                        .cobrancaStatus(CobrancaStatus.PAGA)
                        .dataPagamento(hoje.minusMonths(1))
                        .build(),
                Cobranca.builder()
                        .valor(new BigDecimal("400.00"))
                        .cobrancaStatus(CobrancaStatus.PENDENTE)
                        .dataVencimento(hoje.plusDays(5))
                        .build()
        );

        when(cobrancaRepository.findByContrato_Imovel_Locador_IdUsuario(locadorId)).thenReturn(cobrancas);
        when(imovelRepository.findByLocadorIdUsuario(locadorId)).thenReturn(List.of());
        when(manutencaoRepository.findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(locadorId))
                .thenReturn(List.of(PedidoManutencao.builder()
                        .dataAbertura(hoje)
                        .valorGasto(new BigDecimal("200.00"))
                        .build()));

        var resumo = dashboardService.buscarResumo(Usuario.builder().idUsuario(locadorId).build());

        assertThat(resumo.recebidoMes()).isEqualByComparingTo("1250.00");
        assertThat(resumo.totalPendente()).isEqualByComparingTo("400.00");
        assertThat(resumo.fluxoCaixa()).hasSize(12);
        assertThat(resumo.fluxoCaixa().get(11).mes()).isEqualTo(YearMonth.from(hoje).toString());
        assertThat(resumo.fluxoCaixa().get(11).recebido()).isEqualByComparingTo("1250.00");
        assertThat(resumo.fluxoCaixa().get(10).recebido()).isEqualByComparingTo("900.00");
        assertThat(resumo.fluxoCaixa().get(11).gastosReformas()).isEqualByComparingTo("200.00");
        assertThat(resumo.fluxoCaixa().get(11).inadimplencia()).isEqualByComparingTo("0.00");
    }
}