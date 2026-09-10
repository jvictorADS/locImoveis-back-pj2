package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.contrato.ContratoRequestDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoResponseDTO;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.mapper.ContratoMapper;
import com.locImoveis.pj2back.repository.ContratoRepository;
import com.locImoveis.pj2back.repository.ImovelRepository;
import com.locImoveis.pj2back.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper contratoMapper;
    private final UsuarioRepository usuarioRepository;
    private final ImovelRepository imovelRepository;

    @Transactional
    public ContratoResponseDTO criarContrato(ContratoRequestDTO contratoDTO) {
        Imovel imovel = imovelRepository.findById(contratoDTO.imovelId())
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado!"));

        if(imovel.getOcupacaoStatus() != OcupacaoStatus.DISPONIVEL) {
            throw new IllegalStateException("Imóvel selecionado não está disponível para locação!");
        }

        Usuario locatario = usuarioRepository.findById(contratoDTO.locatarioId())
                .orElseThrow(() -> new IllegalArgumentException("Locatário não encontrado!"));

        Contrato contrato = contratoMapper.toEntity(contratoDTO);
        contrato.setImovel(imovel);
        contrato.setLocatario(locatario);

        contrato.setContratoStatus(ContratoStatus.ATIVO);
        contrato.setDataProximoReajuste(contratoDTO.dataInicio().plusYears(1));

        imovel.setOcupacaoStatus(OcupacaoStatus.ALUGADO);
        imovelRepository.save(imovel);

        Contrato contratoSalvo = contratoRepository.save(contrato);
        return contratoMapper.toDto(contratoSalvo);
    }

    public ContratoResponseDTO buscarPorId(Integer id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado!"));
        return contratoMapper.toDto(contrato);
    }

    public List<ContratoResponseDTO> listarPorImovel(Integer imovelId){
        return contratoRepository.findByImovelIdImovel(imovelId)
                .stream().map(contratoMapper::toDto).collect(Collectors.toList());
    }

}
