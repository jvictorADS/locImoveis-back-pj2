package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.manutencao.ContratoOpcaoDTO;
import com.locImoveis.pj2back.dto.manutencao.ManutencaoRequestDTO;
import com.locImoveis.pj2back.dto.manutencao.ManutencaoResponseDTO;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.PedidoManutencao;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.PedidoStatus;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.repository.ContratoRepository;
import com.locImoveis.pj2back.repository.PedidoManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoManutencaoService {

    private final PedidoManutencaoRepository manutencaoRepository;
    private final ContratoRepository contratoRepository;

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listar(Usuario locador) {
        exigirLocador(locador);
        return manutencaoRepository
                .findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(locador.getIdUsuario())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ManutencaoResponseDTO buscarPorId(Integer id, Usuario locador) {
        return toResponse(buscarDoLocador(id, locador));
    }

    @Transactional(readOnly = true)
    public List<ContratoOpcaoDTO> listarContratos(Usuario locador) {
        exigirLocador(locador);
        return contratoRepository.findByImovel_Locador_IdUsuarioOrderByDataInicioDesc(locador.getIdUsuario())
                .stream()
                .map(contrato -> new ContratoOpcaoDTO(
                        contrato.getIdContrato(),
                        contrato.getLocatario().getNomeCompleto(),
                        contrato.getImovel().getEndereco()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ManutencaoResponseDTO> listarDoLocatario(Usuario locatario) {
        exigirLocatario(locatario);
        return manutencaoRepository
                .findByContrato_Locatario_IdUsuarioOrderByDataAberturaDesc(locatario.getIdUsuario())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ManutencaoResponseDTO criar(ManutencaoRequestDTO dto, Usuario locador) {
        exigirLocador(locador);
        Contrato contrato = buscarContratoDoLocador(dto.contratoId(), locador);
        PedidoManutencao manutencao = PedidoManutencao.builder()
                .descricaoProblema(dto.descricaoProblema().trim())
            .valorGasto(dto.valorGasto())
                .prioridade(dto.prioridade())
                .contrato(contrato)
                .build();
        return toResponse(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO atualizar(Integer id, ManutencaoRequestDTO dto, Usuario locador) {
        exigirLocador(locador);
        PedidoManutencao manutencao = buscarDoLocador(id, locador);
        manutencao.setContrato(buscarContratoDoLocador(dto.contratoId(), locador));
        manutencao.setDescricaoProblema(dto.descricaoProblema().trim());
        manutencao.setValorGasto(dto.valorGasto());
        manutencao.setPrioridade(dto.prioridade());
        return toResponse(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public ManutencaoResponseDTO atualizarStatus(Integer id, PedidoStatus status, Usuario locador) {
        exigirLocador(locador);
        if (status != PedidoStatus.EM_ANDAMENTO && status != PedidoStatus.RESOLVIDO) {
            throw new IllegalArgumentException("O status deve ser EM_ANDAMENTO ou RESOLVIDO.");
        }
        PedidoManutencao manutencao = buscarDoLocador(id, locador);
        manutencao.setStatus(status);
        return toResponse(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public void excluir(Integer id, Usuario locador) {
        exigirLocador(locador);
        manutencaoRepository.delete(buscarDoLocador(id, locador));
    }

    private PedidoManutencao buscarDoLocador(Integer id, Usuario locador) {
        exigirLocador(locador);
        PedidoManutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manutenção não encontrada."));
        if (!manutencao.getContrato().getImovel().getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Manutenção não encontrada.");
        }
        return manutencao;
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

    private ManutencaoResponseDTO toResponse(PedidoManutencao manutencao) {
        Contrato contrato = manutencao.getContrato();
        return new ManutencaoResponseDTO(
                manutencao.getIdPedido(),
                manutencao.getDataAbertura(),
                manutencao.getDescricaoProblema(),
                manutencao.getValorGasto(),
                manutencao.getPrioridade(),
                manutencao.getStatus(),
                contrato.getIdContrato(),
                contrato.getLocatario().getNomeCompleto(),
                contrato.getImovel().getEndereco()
        );
    }

    private void exigirLocador(Usuario usuario) {
        if (usuario == null || usuario.getTipoUsuario() != TipoUsuario.LOCADOR) {
            throw new AccessDeniedException("Operação disponível somente para locadores.");
        }
    }

    private void exigirLocatario(Usuario usuario) {
        if (usuario == null || usuario.getTipoUsuario() != TipoUsuario.LOCATARIO) {
            throw new AccessDeniedException("Operação disponível somente para locatários.");
        }
    }
}