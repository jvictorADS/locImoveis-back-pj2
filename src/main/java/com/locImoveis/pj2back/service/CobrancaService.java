package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.cobranca.CobrancaRequestDTO;
import com.locImoveis.pj2back.dto.cobranca.CobrancaResponseDTO;
import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.mapper.CobrancaMapper;
import com.locImoveis.pj2back.repository.CobrancaRepository;
import com.locImoveis.pj2back.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final CobrancaMapper cobrancaMapper;
    private final ContratoRepository contratoRepository;

    @Transactional
    public CobrancaResponseDTO criarCobrança(CobrancaRequestDTO cobrancaDTO, Usuario locador) {
        Contrato contrato = buscarContratoDoLocador(cobrancaDTO.contratoId(), locador);

        if (contrato.getContratoStatus() != ContratoStatus.ATIVO) { // Ajuste conforme seu Enum
            throw new IllegalStateException("Não é possível gerar cobranças para um contrato que não está ativo.");
        }

        Cobranca cobranca = cobrancaMapper.toEntity(cobrancaDTO);
        cobranca.setContrato(contrato);
        // toda cobrança nasce pendente
        cobranca.setCobrancaStatus(CobrancaStatus.PENDENTE);

        Cobranca cobrancaSalva = cobrancaRepository.save(cobranca);
        return cobrancaMapper.toDto(cobrancaSalva);
    }

    @Transactional
    public List<CobrancaResponseDTO> gerarParcelas(Integer contratoId, Usuario locador) {
        Contrato contrato = buscarContratoDoLocador(contratoId, locador);
        YearMonth primeiroMes = YearMonth.from(contrato.getDataInicio());
        YearMonth ultimoMes = YearMonth.from(contrato.getDataFim());
        if (ultimoMes.isBefore(primeiroMes)) {
            throw new IllegalStateException("A vigência do contrato não possui meses válidos.");
        }

        List<Cobranca> existentes = cobrancaRepository
                .findByContratoIdContratoOrderByDataVencimentoAsc(contratoId);
        Set<YearMonth> mesesExistentes = existentes.stream()
                .map(cobranca -> YearMonth.from(cobranca.getDataVencimento()))
                .collect(Collectors.toCollection(HashSet::new));
        List<Cobranca> novasParcelas = new java.util.ArrayList<>();

        for (YearMonth mes = primeiroMes; !mes.isAfter(ultimoMes); mes = mes.plusMonths(1)) {
            if (mesesExistentes.add(mes)) {
                int diaVencimento = Math.min(contrato.getDiaVencimento(), mes.lengthOfMonth());
                LocalDate vencimento = mes.atDay(diaVencimento);
                if (mes.equals(primeiroMes) && vencimento.isBefore(contrato.getDataInicio())) {
                    vencimento = contrato.getDataInicio();
                }

                novasParcelas.add(Cobranca.builder()
                        .mesReferencia(mes.toString())
                        .dataVencimento(vencimento)
                        .valor(contrato.getValorAcordado())
                        .cobrancaStatus(CobrancaStatus.PENDENTE)
                        .contrato(contrato)
                        .build());
            }
        }

        if (!novasParcelas.isEmpty()) {
            cobrancaRepository.saveAll(novasParcelas);
        }

        return cobrancaRepository.findByContratoIdContratoOrderByDataVencimentoAsc(contratoId)
                .stream()
                .map(cobrancaMapper::toDto)
                .toList();
    }

    @Transactional
    public CobrancaResponseDTO registrarPagamento(Integer id, Usuario locador) {
        Cobranca cobranca = buscarCobrancaDoLocador(id, locador);

        if (cobranca.getCobrancaStatus() == CobrancaStatus.PAGA) {
            throw new IllegalStateException("Essa cobrança já consta como paga!");
        }
        if (cobranca.getCobrancaStatus() == CobrancaStatus.CANCELADA) {
            throw new IllegalStateException("Não é possível pagar uma cobrança cancelada.");
        }

        cobranca.setCobrancaStatus(CobrancaStatus.PAGA);
        cobranca.setDataPagamento(LocalDate.now());
        return cobrancaMapper.toDto(cobrancaRepository.save(cobranca));
    }

    @Transactional
    public CobrancaResponseDTO marcarPendente(Integer id, Usuario locador) {
        Cobranca cobranca = buscarCobrancaDoLocador(id, locador);
        if (cobranca.getCobrancaStatus() != CobrancaStatus.PAGA) {
            throw new IllegalStateException("Somente cobranças pagas podem voltar para pendente.");
        }

        cobranca.setCobrancaStatus(CobrancaStatus.PENDENTE);
        cobranca.setDataPagamento(null);
        return cobrancaMapper.toDto(cobrancaRepository.save(cobranca));
    }

    @Transactional(readOnly = true)
    public CobrancaResponseDTO buscarPorId(Integer id, Usuario locador){
        Cobranca cobranca = buscarCobrancaDoLocador(id, locador);
        return cobrancaMapper.toDto(cobranca);
    }

    @Transactional(readOnly = true)
    public List<CobrancaResponseDTO> listarPorContrato(Integer contratoId, Usuario locador){
        buscarContratoDoLocador(contratoId, locador);
        return cobrancaRepository.findByContratoIdContrato(contratoId)
                .stream().map(cobrancaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CobrancaResponseDTO> listarPorStatus(CobrancaStatus cobrancaStatus, Usuario locador){
        exigirLocador(locador);
        return cobrancaRepository.findByContrato_Imovel_Locador_IdUsuarioAndCobrancaStatus(
                        locador.getIdUsuario(), cobrancaStatus)
                .stream().map(cobrancaMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void cancelarCobranca(Integer id, Usuario locador){
        Cobranca cobranca = buscarCobrancaDoLocador(id, locador);

        if(cobranca.getCobrancaStatus() == CobrancaStatus.PAGA) {
            throw new IllegalStateException("Não é possível cancelar uma cobrança já paga!");
        }

        cobranca.setCobrancaStatus(CobrancaStatus.CANCELADA);
        cobrancaRepository.save(cobranca);
    }

    private Contrato buscarContratoDoLocador(Integer contratoId, Usuario locador) {
        exigirLocador(locador);
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado."));
        if (!contrato.getImovel().getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Contrato não encontrado.");
        }
        return contrato;
    }

    private Cobranca buscarCobrancaDoLocador(Integer id, Usuario locador) {
        exigirLocador(locador);
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada."));
        if (!cobranca.getContrato().getImovel().getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Cobrança não encontrada.");
        }
        return cobranca;
    }

    private void exigirLocador(Usuario locador) {
        if (locador == null || locador.getTipoUsuario() != TipoUsuario.LOCADOR) {
            throw new AccessDeniedException("Operação disponível somente para proprietários.");
        }
    }

}
