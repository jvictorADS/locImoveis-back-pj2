package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.contrato.ContratoRequestDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoResponseDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoUpdateDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        if (contratoDTO.imovelId() == null || contratoDTO.locatarioId() == null) {
            throw new IllegalArgumentException("Imóvel e Locatário devem ser informados.");
        }

        if (contratoDTO.dataInicio() == null || contratoDTO.dataFim() == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }

        if (!contratoDTO.dataFim().isAfter(contratoDTO.dataInicio())) {
            throw new IllegalArgumentException("A data de término deve ser posterior à data de início.");
        }

        Imovel imovel = imovelRepository.findById(contratoDTO.imovelId())
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado!"));

        if (imovel.getOcupacaoStatus() != OcupacaoStatus.DISPONIVEL) {
            throw new IllegalStateException("Imóvel selecionado não está disponível para locação!");
        }

        Usuario locatario = usuarioRepository.findById(contratoDTO.locatarioId())
                .orElseThrow(() -> new IllegalArgumentException("Locatário não encontrado!"));

        Contrato contrato = contratoMapper.toEntity(contratoDTO);
        contrato.setImovel(imovel);
        contrato.setLocatario(locatario);

        if (contrato.getDiaVencimento() == null) {
            contrato.setDiaVencimento(
                    contratoDTO.diaVencimento() != null ? contratoDTO.diaVencimento() : contratoDTO.dataInicio().getDayOfMonth()
            );
        }

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

    public List<ContratoResponseDTO> listarPorImovel(Integer imovelId) {
        return contratoRepository.findByImovelIdImovel(imovelId)
                .stream().map(contratoMapper::toDto).collect(Collectors.toList());
    }

    public List<ContratoResponseDTO> listarPorLocatario(Integer locatarioId) {
        return contratoRepository.findByLocatarioIdUsuario(locatarioId)
                .stream().map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ContratoResponseDTO> listarTodos() {
        return contratoRepository.findAll()
                .stream()
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContratoResponseDTO rescindirContrato(Integer id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado!"));
        contrato.setContratoStatus(ContratoStatus.RESCINDIDO);

        Imovel imovel = contrato.getImovel();
        if (imovel != null) {
            imovel.setOcupacaoStatus(OcupacaoStatus.DISPONIVEL);
            imovelRepository.save(imovel);
        }

        return contratoMapper.toDto(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponseDTO atualizarContrato(Integer id, ContratoUpdateDTO dto) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado!"));

        if (dto.dataInicio() != null) {
            contrato.setDataInicio(dto.dataInicio());
        }

        if (dto.dataFim() != null) {
            contrato.setDataFim(dto.dataFim());
            if (contrato.getDataFim().isBefore(contrato.getDataInicio())) {
                throw new IllegalArgumentException("A data de término não pode ser anterior à data de início.");
            }
        }

        if (dto.valorAcordado() != null) {
            contrato.setValorAcordado(dto.valorAcordado());
        }

        if (dto.contratoStatus() != null) {
            ContratoStatus novoStatus = ContratoStatus.valueOf(dto.contratoStatus());

            if (contrato.getContratoStatus() == ContratoStatus.ATIVO &&
                    (novoStatus == ContratoStatus.RESCINDIDO || novoStatus == ContratoStatus.ENCERRADO)) {

                Imovel imovel = contrato.getImovel();
                if (imovel != null) {
                    imovel.setOcupacaoStatus(OcupacaoStatus.DISPONIVEL);
                    imovelRepository.save(imovel);
                }
            }
            // Se voltou a ser ativo, bloqueia o imóvel
            else if (contrato.getContratoStatus() != ContratoStatus.ATIVO && novoStatus == ContratoStatus.ATIVO) {
                Imovel imovel = contrato.getImovel();
                if (imovel != null) {
                    imovel.setOcupacaoStatus(OcupacaoStatus.ALUGADO);
                    imovelRepository.save(imovel);
                }
            }

            contrato.setContratoStatus(novoStatus);
        }

        Contrato contratoAtualizado = contratoRepository.save(contrato);
        return contratoMapper.toDto(contratoAtualizado);
    }


    @Transactional
    public ContratoResponseDTO uploadDocumento(Integer id, MultipartFile arquivo) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado!"));

        try {
            contrato.setArquivoPdf(arquivo.getBytes());
            Contrato contratoAtualizado = contratoRepository.save(contrato);
            return contratoMapper.toDto(contratoAtualizado);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo PDF", e);
        }
    }

    public byte[] obterDocumento(Integer id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado!"));

        if (contrato.getArquivoPdf() == null || contrato.getArquivoPdf().length == 0) {
            throw new IllegalStateException("Nenhum arquivo PDF anexado a este contrato.");
        }

        return contrato.getArquivoPdf();
    }
}