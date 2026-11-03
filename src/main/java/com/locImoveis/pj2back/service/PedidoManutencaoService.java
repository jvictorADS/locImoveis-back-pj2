package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.manutencao.ContratoOpcaoDTO;
import com.locImoveis.pj2back.dto.manutencao.ManutencaoRequestDTO;
import com.locImoveis.pj2back.dto.manutencao.ManutencaoResponseDTO;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.PedidoManutencao;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.repository.ContratoRepository;
import com.locImoveis.pj2back.repository.PedidoManutencaoRepository;
import lombok.RequiredArgsConstructor;
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
        return contratoRepository.findByImovel_Locador_IdUsuarioOrderByDataInicioDesc(locador.getIdUsuario())
                .stream()
                .map(contrato -> new ContratoOpcaoDTO(
                        contrato.getIdContrato(),
                        contrato.getLocatario().getNomeCompleto(),
                        contrato.getImovel().getEndereco()
                ))
                .toList();
    }

    @Transactional
    public ManutencaoResponseDTO criar(ManutencaoRequestDTO dto, Usuario locador) {
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
        PedidoManutencao manutencao = buscarDoLocador(id, locador);
        manutencao.setContrato(buscarContratoDoLocador(dto.contratoId(), locador));
        manutencao.setDescricaoProblema(dto.descricaoProblema().trim());
        manutencao.setValorGasto(dto.valorGasto());
        manutencao.setPrioridade(dto.prioridade());
        return toResponse(manutencaoRepository.save(manutencao));
    }

    @Transactional
    public void excluir(Integer id, Usuario locador) {
        manutencaoRepository.delete(buscarDoLocador(id, locador));
    }

    private PedidoManutencao buscarDoLocador(Integer id, Usuario locador) {
        PedidoManutencao manutencao = manutencaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Manutenção não encontrada."));
        if (!manutencao.getContrato().getImovel().getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new IllegalArgumentException("Manutenção não encontrada.");
        }
        return manutencao;
    }

    private Contrato buscarContratoDoLocador(Integer contratoId, Usuario locador) {
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
                contrato.getIdContrato(),
                contrato.getLocatario().getNomeCompleto(),
                contrato.getImovel().getEndereco()
        );
    }
}