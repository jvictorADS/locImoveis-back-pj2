package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.contrato.ContratoRequestDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoResponseDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoUpdateDTO;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.mapper.ContratoMapper;
import com.locImoveis.pj2back.repository.ContratoRepository;
import com.locImoveis.pj2back.repository.ImovelRepository;
import com.locImoveis.pj2back.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    public ContratoResponseDTO criarContrato(ContratoRequestDTO contratoDTO, Usuario locador) {
        exigirLocador(locador);
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
        if (!imovel.getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Imóvel não encontrado!");
        }

        if (imovel.getOcupacaoStatus() != OcupacaoStatus.DISPONIVEL) {
            throw new IllegalStateException("Imóvel selecionado não está disponível para locação!");
        }

        Usuario locatario = usuarioRepository.findById(contratoDTO.locatarioId())
                .orElseThrow(() -> new IllegalArgumentException("Locatário não encontrado!"));
        if (locatario.getTipoUsuario() != TipoUsuario.LOCATARIO
                || !usuarioRepository.pertenceAoLocador(
                        locatario.getIdUsuario(), locador.getIdUsuario(), TipoUsuario.LOCATARIO)) {
            throw new IllegalArgumentException("Locatário não encontrado!");
        }

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

    @Transactional
    public ContratoResponseDTO buscarPorId(Integer id, Usuario locador) {
        Contrato contrato = buscarDoLocador(id, locador);
        return contratoMapper.toDto(contrato);
    }

    @Transactional
    public List<ContratoResponseDTO> listarPorImovel(Integer imovelId, Usuario locador) {
        buscarImovelDoLocador(imovelId, locador);
        return contratoRepository.findByImovelIdImovel(imovelId)
                .stream().map(contratoMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public List<ContratoResponseDTO> listarPorLocatario(Integer locatarioId, Usuario locador) {
        exigirLocador(locador);
        if (!usuarioRepository.pertenceAoLocador(locatarioId, locador.getIdUsuario(), TipoUsuario.LOCATARIO)) {
            throw new IllegalArgumentException("Locatário não encontrado!");
        }
        return contratoRepository.findByLocatarioIdUsuario(locatarioId)
                .stream()
                .filter(contrato -> contrato.getImovel().getLocador().getIdUsuario().equals(locador.getIdUsuario()))
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ContratoResponseDTO> listarTodos(Usuario locador) {
        exigirLocador(locador);
        return contratoRepository.findByImovel_Locador_IdUsuarioOrderByDataInicioDesc(locador.getIdUsuario())
                .stream()
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContratoResponseDTO rescindirContrato(Integer id, Usuario locador) {
        Contrato contrato = buscarDoLocador(id, locador);
        contrato.setContratoStatus(ContratoStatus.RESCINDIDO);

        Imovel imovel = contrato.getImovel();
        if (imovel != null) {
            imovel.setOcupacaoStatus(OcupacaoStatus.DISPONIVEL);
            imovelRepository.save(imovel);
        }

        return contratoMapper.toDto(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponseDTO atualizarContrato(Integer id, ContratoUpdateDTO dto, Usuario locador) {
        Contrato contrato = buscarDoLocador(id, locador);

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
    public ContratoResponseDTO uploadDocumento(Integer id, MultipartFile arquivo, Usuario locador) {
        Contrato contrato = buscarDoLocador(id, locador);

        try {
            contrato.setArquivoPdf(arquivo.getBytes());
            Contrato contratoAtualizado = contratoRepository.save(contrato);
            return contratoMapper.toDto(contratoAtualizado);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo PDF", e);
        }
    }

    @Transactional
    public byte[] obterDocumento(Integer id, Usuario locador) {
        Contrato contrato = buscarDoLocador(id, locador);

        if (contrato.getArquivoPdf() == null || contrato.getArquivoPdf().length == 0) {
            throw new IllegalStateException("Nenhum arquivo PDF anexado a este contrato.");
        }

        return contrato.getArquivoPdf();
    }

    private Contrato buscarDoLocador(Integer id, Usuario locador) {
        exigirLocador(locador);
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado."));
        if (!contrato.getImovel().getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Contrato não encontrado.");
        }
        return contrato;
    }

    private Imovel buscarImovelDoLocador(Integer id, Usuario locador) {
        exigirLocador(locador);
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado."));
        if (!imovel.getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Imóvel não encontrado.");
        }
        return imovel;
    }

    private void exigirLocador(Usuario locador) {
        if (locador == null || locador.getTipoUsuario() != TipoUsuario.LOCADOR) {
            throw new AccessDeniedException("Operação disponível somente para proprietários.");
        }
    }
}