package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.dashboard.DashboardResponseDTO;
import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.PedidoManutencao;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.repository.CobrancaRepository;
import com.locImoveis.pj2back.repository.ContratoRepository;
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
    private ContratoRepository contratoRepository;

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

    @Test
    void aplicaPeriodoNosDadosDoDashboard() {
        Integer locadorId = 12;
        LocalDate hoje = LocalDate.now();
        when(cobrancaRepository.findByContrato_Imovel_Locador_IdUsuario(locadorId)).thenReturn(List.of(
                Cobranca.builder()
                        .valor(new BigDecimal("100.00"))
                        .cobrancaStatus(CobrancaStatus.PAGA)
                        .dataPagamento(hoje)
                        .dataVencimento(hoje)
                        .build(),
                Cobranca.builder()
                        .valor(new BigDecimal("50.00"))
                        .cobrancaStatus(CobrancaStatus.PAGA)
                        .dataPagamento(hoje.minusDays(1))
                        .dataVencimento(hoje.minusDays(1))
                        .build(),
                Cobranca.builder()
                        .valor(new BigDecimal("200.00"))
                        .cobrancaStatus(CobrancaStatus.PENDENTE)
                        .dataVencimento(hoje)
                        .build(),
                Cobranca.builder()
                        .valor(new BigDecimal("80.00"))
                        .cobrancaStatus(CobrancaStatus.PENDENTE)
                        .dataVencimento(hoje.minusDays(1))
                        .build()
        ));
        when(imovelRepository.findByLocadorIdUsuario(locadorId)).thenReturn(List.of());
        when(manutencaoRepository.findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(locadorId))
                .thenReturn(List.of(
                        PedidoManutencao.builder()
                                .dataAbertura(hoje)
                                .valorGasto(new BigDecimal("40.00"))
                                .build(),
                        PedidoManutencao.builder()
                                .dataAbertura(hoje.minusDays(1))
                                .valorGasto(new BigDecimal("90.00"))
                                .build()
                ));

        var resumo = dashboardService.buscarResumo(
                Usuario.builder().idUsuario(locadorId).build(),
                hoje, hoje, null, null, null);

        assertThat(resumo.recebidoMes()).isEqualByComparingTo("100.00");
        assertThat(resumo.totalPendente()).isEqualByComparingTo("200.00");
        assertThat(resumo.gastosReformas()).isEqualByComparingTo("40.00");
        assertThat(resumo.fluxoCaixa()).containsExactly(
                new DashboardResponseDTO.FluxoCaixaMensalDTO(
                        YearMonth.from(hoje).toString(),
                        new BigDecimal("100.00"),
                        new BigDecimal("40.00"),
                        BigDecimal.ZERO));
    }

    @Test
    void filtraDadosPorInquilinoImovelEStatus() {
        Integer locadorId = 12;
        Integer locatarioId = 34;
        Integer imovelId = 56;
        LocalDate hoje = LocalDate.now();
        Imovel imovel = Imovel.builder()
                .idImovel(imovelId)
                .ocupacaoStatus(OcupacaoStatus.ALUGADO)
                .build();
        Contrato contrato = Contrato.builder()
                .imovel(imovel)
                .locatario(Usuario.builder().idUsuario(locatarioId).build())
                .contratoStatus(ContratoStatus.ATIVO)
                .dataInicio(hoje.minusMonths(1))
                .dataFim(hoje.plusMonths(1))
                .build();
        Cobranca cobrancaDoInquilino = Cobranca.builder()
                .contrato(contrato)
                .valor(new BigDecimal("500.00"))
                .cobrancaStatus(CobrancaStatus.PAGA)
                .dataPagamento(hoje)
                .dataVencimento(hoje)
                .build();
        Cobranca cobrancaDeOutroImovel = Cobranca.builder()
                .contrato(Contrato.builder()
                        .imovel(Imovel.builder().idImovel(99).build())
                        .locatario(Usuario.builder().idUsuario(100).build())
                        .build())
                .valor(new BigDecimal("900.00"))
                .cobrancaStatus(CobrancaStatus.PAGA)
                .dataPagamento(hoje)
                .dataVencimento(hoje)
                .build();

        when(contratoRepository
                .findByImovel_Locador_IdUsuarioAndLocatario_IdUsuarioAndContratoStatus(
                        locadorId, locatarioId, ContratoStatus.ATIVO))
                .thenReturn(List.of(contrato));
        when(cobrancaRepository.findByContrato_Imovel_Locador_IdUsuario(locadorId))
                .thenReturn(List.of(cobrancaDoInquilino, cobrancaDeOutroImovel));
        when(imovelRepository.findByLocadorIdUsuario(locadorId)).thenReturn(List.of(imovel));
        when(manutencaoRepository.findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(locadorId))
                .thenReturn(List.of());

        var resumo = dashboardService.buscarResumo(
                Usuario.builder().idUsuario(locadorId).build(),
                null, null, locatarioId, imovelId, CobrancaStatus.PAGA);

        assertThat(resumo.recebidoMes()).isEqualByComparingTo("500.00");
        assertThat(resumo.imoveisAlugados()).isEqualTo(1);
        assertThat(resumo.imoveisVagos()).isZero();
    }
}