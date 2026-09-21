package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.cobranca.CobrancaRequestDTO;
import com.locImoveis.pj2back.dto.cobranca.CobrancaResponseDTO;
import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.mapper.CobrancaMapper;
import com.locImoveis.pj2back.repository.CobrancaRepository;
import com.locImoveis.pj2back.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final CobrancaMapper cobrancaMapper;
    private final ContratoRepository contratoRepository;

    @Transactional
    public CobrancaResponseDTO criarCobrança(CobrancaRequestDTO cobrancaDTO) {
        Contrato contrato = contratoRepository.findById(cobrancaDTO.contratoId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado!"));

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
    public CobrancaResponseDTO registrarPagamento(Integer id){
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cobranca inexistente!"));

        if(cobranca.getCobrancaStatus() == CobrancaStatus.PAGA) {
            throw new IllegalStateException("Essa cobrança já consta como paga!");
        }

        cobranca.setCobrancaStatus(CobrancaStatus.PAGA);
        cobranca.setDataPagamento(LocalDate.now());
        return cobrancaMapper.toDto(cobrancaRepository.save(cobranca));
    }

    public CobrancaResponseDTO buscarPorId(Integer id){
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cobranca não encontrada!"));
        return cobrancaMapper.toDto(cobranca);
    }

    public List<CobrancaResponseDTO> listarPorContrato(Integer contratoId){
        return cobrancaRepository.findByContratoIdContrato(contratoId)
                .stream().map(cobrancaMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CobrancaResponseDTO> listarPorStatus(CobrancaStatus cobrancaStatus){
        return cobrancaRepository.findByCobrancaStatus(cobrancaStatus)
                .stream().map(cobrancaMapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void cancelarCobranca(Integer id){
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cobranca não encontrada!"));

        if(cobranca.getCobrancaStatus() == CobrancaStatus.PAGA) {
            throw new IllegalStateException("Não é possível cancelar uma cobrança já paga!");
        }

        cobranca.setCobrancaStatus(CobrancaStatus.CANCELADA);
        cobrancaRepository.save(cobranca);
    }

}
