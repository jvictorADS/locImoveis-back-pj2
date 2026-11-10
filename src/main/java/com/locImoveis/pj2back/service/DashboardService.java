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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CobrancaRepository cobrancaRepository;
    private final ContratoRepository contratoRepository;
    private final ImovelRepository imovelRepository;
    private final PedidoManutencaoRepository manutencaoRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO buscarResumo(Usuario locador) {
        return buscarResumo(locador, null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO buscarResumo(
            Usuario locador,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer locatarioId,
            Integer imovelId,
            CobrancaStatus status
    ) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        Set<Integer> imoveisDoLocatario = locatarioId == null
                ? null
                : contratosAtivos(locador.getIdUsuario(), locatarioId, hoje).stream()
                        .map(contrato -> contrato.getImovel().getIdImovel())
                        .collect(HashSet::new, HashSet::add, HashSet::addAll);

        List<Cobranca> cobrancas = cobrancaRepository
                .findByContrato_Imovel_Locador_IdUsuario(locador.getIdUsuario());
        cobrancas = cobrancas.stream()
                .filter(cobranca -> status == null || cobranca.getCobrancaStatus() == status)
                .filter(cobranca -> locatarioId == null
                        || cobranca.getContrato().getLocatario().getIdUsuario().equals(locatarioId))
                .filter(cobranca -> imovelId == null
                        || cobranca.getContrato().getImovel().getIdImovel().equals(imovelId))
                .filter(cobranca -> estaNoPeriodo(
                        cobranca.getCobrancaStatus() == CobrancaStatus.PAGA
                                ? cobranca.getDataPagamento()
                                : cobranca.getDataVencimento(),
                        dataInicio, dataFim))
                .toList();

        List<Imovel> imoveis = imovelRepository.findByLocadorIdUsuario(locador.getIdUsuario()).stream()
                .filter(imovel -> imovelId == null || imovel.getIdImovel().equals(imovelId))
                .filter(imovel -> imoveisDoLocatario == null || imoveisDoLocatario.contains(imovel.getIdImovel()))
                .toList();
        List<PedidoManutencao> manutencoes = manutencaoRepository
                .findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(locador.getIdUsuario());
        manutencoes = manutencoes.stream()
                .filter(manutencao -> locatarioId == null
                        || manutencao.getContrato().getLocatario().getIdUsuario().equals(locatarioId))
                .filter(manutencao -> imovelId == null
                        || manutencao.getContrato().getImovel().getIdImovel().equals(imovelId))
                .filter(manutencao -> estaNoPeriodo(manutencao.getDataAbertura(), dataInicio, dataFim))
                .toList();
        YearMonth mesAtual = YearMonth.from(hoje);
        YearMonth primeiroMes = dataInicio != null
                ? YearMonth.from(dataInicio)
                : dataFim != null
                        ? YearMonth.from(dataFim).minusMonths(11)
                        : mesAtual.minusMonths(11);
        YearMonth ultimoMes = dataFim != null ? YearMonth.from(dataFim) : mesAtual;
        if (primeiroMes.isAfter(ultimoMes)) {
            ultimoMes = primeiroMes;
        }
        Map<YearMonth, FluxoCaixaMensal> fluxoCaixaPorMes = new LinkedHashMap<>();
        for (YearMonth mes = primeiroMes; !mes.isAfter(ultimoMes); mes = mes.plusMonths(1)) {
            fluxoCaixaPorMes.put(mes, new FluxoCaixaMensal());
        }

        BigDecimal recebidoMes = cobrancas.stream()
                .filter(cobranca -> cobranca.getCobrancaStatus() == CobrancaStatus.PAGA)
                .filter(cobranca -> cobranca.getDataPagamento() != null
                        && (dataInicio != null || dataFim != null
                                || !cobranca.getDataPagamento().isBefore(inicioMes))
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

    private List<Contrato> contratosAtivos(Integer locadorId, Integer locatarioId, LocalDate hoje) {
        return contratoRepository
                .findByImovel_Locador_IdUsuarioAndLocatario_IdUsuarioAndContratoStatus(
                        locadorId, locatarioId, ContratoStatus.ATIVO)
                .stream()
                .filter(contrato -> !contrato.getDataInicio().isAfter(hoje)
                        && !contrato.getDataFim().isBefore(hoje))
                .toList();
    }

    private boolean estaNoPeriodo(LocalDate data, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return true;
        }
        return data != null
                && (dataInicio == null || !data.isBefore(dataInicio))
                && (dataFim == null || !data.isAfter(dataFim));
    }

    private static class FluxoCaixaMensal {
        private BigDecimal recebido = BigDecimal.ZERO;
        private BigDecimal gastosReformas = BigDecimal.ZERO;
        private BigDecimal inadimplencia = BigDecimal.ZERO;
    }
}