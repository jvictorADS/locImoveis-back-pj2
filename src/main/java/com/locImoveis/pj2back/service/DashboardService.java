package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.dashboard.DashboardResponseDTO;
import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.PedidoManutencao;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.repository.CobrancaRepository;
import com.locImoveis.pj2back.repository.ImovelRepository;
import com.locImoveis.pj2back.repository.PedidoManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CobrancaRepository cobrancaRepository;
    private final ImovelRepository imovelRepository;
    private final PedidoManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO buscarResumo(Usuario locador) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        List<Cobranca> cobrancas = cobrancaRepository
                .findByContrato_Imovel_Locador_IdUsuario(locador.getIdUsuario());
        List<Imovel> imoveis = imovelRepository.findByLocadorIdUsuario(locador.getIdUsuario());
        List<PedidoManutencao> manutencoes = manutencaoRepository
                .findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(locador.getIdUsuario());
        YearMonth mesAtual = YearMonth.from(hoje);
        YearMonth primeiroMes = mesAtual.minusMonths(11);
        Map<YearMonth, FluxoCaixaMensal> fluxoCaixaPorMes = new LinkedHashMap<>();
        for (int mes = 0; mes < 12; mes++) {
            fluxoCaixaPorMes.put(primeiroMes.plusMonths(mes), new FluxoCaixaMensal());
        }

        BigDecimal recebidoMes = cobrancas.stream()
                .filter(cobranca -> cobranca.getCobrancaStatus() == CobrancaStatus.PAGA)
                .filter(cobranca -> cobranca.getDataPagamento() != null
                        && !cobranca.getDataPagamento().isBefore(inicioMes)
                        && !cobranca.getDataPagamento().isAfter(hoje))
                .map(Cobranca::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cobrancas.forEach(cobranca -> {
            if (cobranca.getCobrancaStatus() == CobrancaStatus.PAGA && cobranca.getDataPagamento() != null) {
                FluxoCaixaMensal dadosMes = fluxoCaixaPorMes.get(YearMonth.from(cobranca.getDataPagamento()));
                if (dadosMes != null) {
                    dadosMes.recebido = dadosMes.recebido.add(cobranca.getValor());
                }
            }

            if ((cobranca.getCobrancaStatus() == CobrancaStatus.PENDENTE
                    || cobranca.getCobrancaStatus() == CobrancaStatus.ATRASADA)
                    && cobranca.getDataVencimento().isBefore(hoje)) {
                FluxoCaixaMensal dadosMes = fluxoCaixaPorMes.get(YearMonth.from(cobranca.getDataVencimento()));
                if (dadosMes != null) {
                    dadosMes.inadimplencia = dadosMes.inadimplencia.add(cobranca.getValor());
                }
            }
        });

        manutencoes.stream()
                .filter(manutencao -> manutencao.getDataAbertura() != null && manutencao.getValorGasto() != null)
                .forEach(manutencao -> {
                    FluxoCaixaMensal dadosMes = fluxoCaixaPorMes.get(YearMonth.from(manutencao.getDataAbertura()));
                    if (dadosMes != null) {
                        dadosMes.gastosReformas = dadosMes.gastosReformas.add(manutencao.getValorGasto());
                    }
                });

        BigDecimal totalPendente = cobrancas.stream()
                .filter(cobranca -> cobranca.getCobrancaStatus() == CobrancaStatus.PENDENTE
                        || cobranca.getCobrancaStatus() == CobrancaStatus.ATRASADA)
                .map(Cobranca::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BigDecimal> valoresReformas = manutencoes.stream()
                .map(PedidoManutencao::getValorGasto)
                .filter(Objects::nonNull)
                .toList();
        BigDecimal gastosReformas = valoresReformas.isEmpty()
                ? null
                : valoresReformas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardResponseDTO.InadimplenteDTO> inadimplentes = cobrancas.stream()
                .filter(cobranca -> cobranca.getCobrancaStatus() == CobrancaStatus.PENDENTE
                        || cobranca.getCobrancaStatus() == CobrancaStatus.ATRASADA)
                .filter(cobranca -> cobranca.getDataVencimento().isBefore(hoje))
                .sorted(Comparator.comparing(Cobranca::getDataVencimento))
                .map(cobranca -> new DashboardResponseDTO.InadimplenteDTO(
                        cobranca.getIdCobranca(),
                        cobranca.getContrato().getLocatario().getNomeCompleto(),
                        cobranca.getContrato().getImovel().getEndereco(),
                        ChronoUnit.DAYS.between(cobranca.getDataVencimento(), hoje),
                        cobranca.getValor()
                ))
                .toList();

        long imoveisAlugados = imoveis.stream()
                .filter(imovel -> imovel.getOcupacaoStatus() == OcupacaoStatus.ALUGADO)
                .count();
        long imoveisVagos = imoveis.stream()
                .filter(imovel -> imovel.getOcupacaoStatus() == OcupacaoStatus.DISPONIVEL)
                .count();

        return new DashboardResponseDTO(
                recebidoMes,
                gastosReformas,
                totalPendente,
                imoveisAlugados,
                imoveisVagos,
                inadimplentes,
                fluxoCaixaPorMes.entrySet().stream()
                        .map(entry -> new DashboardResponseDTO.FluxoCaixaMensalDTO(
                                entry.getKey().toString(),
                                entry.getValue().recebido,
                                entry.getValue().gastosReformas,
                                entry.getValue().inadimplencia
                        ))
                        .toList()
        );
    }

    private static class FluxoCaixaMensal {
        private BigDecimal recebido = BigDecimal.ZERO;
        private BigDecimal gastosReformas = BigDecimal.ZERO;
        private BigDecimal inadimplencia = BigDecimal.ZERO;
    }
}