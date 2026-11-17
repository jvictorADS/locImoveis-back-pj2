package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.PedidoManutencao;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.PedidoStatus;
import com.locImoveis.pj2back.entity.enums.Prioridade;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.repository.ContratoRepository;
import com.locImoveis.pj2back.repository.PedidoManutencaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoManutencaoServiceTest {

    @Mock
    private PedidoManutencaoRepository manutencaoRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private PedidoManutencaoService manutencaoService;

    @Test
    void locadorAtualizaStatusDoChamado() {
        PedidoManutencao chamado = criarChamado();
        when(manutencaoRepository.findById(30)).thenReturn(Optional.of(chamado));
        when(manutencaoRepository.save(chamado)).thenReturn(chamado);

        var resposta = manutencaoService.atualizarStatus(
                30, PedidoStatus.EM_ANDAMENTO, usuario(10, TipoUsuario.LOCADOR));

        assertThat(chamado.getStatus()).isEqualTo(PedidoStatus.EM_ANDAMENTO);
        assertThat(resposta.status()).isEqualTo(PedidoStatus.EM_ANDAMENTO);
        verify(manutencaoRepository).save(chamado);
    }

    @Test
    void locatarioConsultaSomenteChamadosVinculadosAContaAutenticada() {
        PedidoManutencao chamado = criarChamado();
        when(manutencaoRepository.findByContrato_Locatario_IdUsuarioOrderByDataAberturaDesc(20))
                .thenReturn(List.of(chamado));

        var resposta = manutencaoService.listarDoLocatario(usuario(20, TipoUsuario.LOCATARIO));

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).status()).isEqualTo(PedidoStatus.ABERTO);
        verify(manutencaoRepository).findByContrato_Locatario_IdUsuarioOrderByDataAberturaDesc(20);
    }

    @Test
    void locadorNaoPodeReabrirChamadoPeloEndpointDeAtualizacao() {
        assertThatThrownBy(() -> manutencaoService.atualizarStatus(
                30, PedidoStatus.ABERTO, usuario(10, TipoUsuario.LOCADOR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O status deve ser EM_ANDAMENTO ou RESOLVIDO.");
    }

    @Test
    void locatarioNaoPodeAlterarStatusDoChamado() {
        assertThatThrownBy(() -> manutencaoService.atualizarStatus(
                30, PedidoStatus.RESOLVIDO, usuario(20, TipoUsuario.LOCATARIO)))
                .isInstanceOf(AccessDeniedException.class);
    }

    private PedidoManutencao criarChamado() {
        Usuario locador = usuario(10, TipoUsuario.LOCADOR);
        Usuario locatario = usuario(20, TipoUsuario.LOCATARIO);
        Imovel imovel = Imovel.builder().endereco("Rua A").locador(locador).build();
        Contrato contrato = Contrato.builder()
                .idContrato(40)
                .imovel(imovel)
                .locatario(locatario)
                .build();
        return PedidoManutencao.builder()
                .idPedido(30)
                .dataAbertura(LocalDate.now())
                .descricaoProblema("Torneira com vazamento")
                .prioridade(Prioridade.MEDIA)
                .status(PedidoStatus.ABERTO)
                .contrato(contrato)
                .build();
    }

    private Usuario usuario(Integer id, TipoUsuario tipo) {
        return Usuario.builder()
                .idUsuario(id)
                .nomeCompleto(tipo == TipoUsuario.LOCADOR ? "Locador" : "Locatario")
                .tipoUsuario(tipo)
                .build();
    }
}
