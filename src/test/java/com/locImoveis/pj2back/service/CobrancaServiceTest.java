package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.cobranca.CobrancaResponseDTO;
import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.mapper.CobrancaMapper;
import com.locImoveis.pj2back.repository.CobrancaRepository;
import com.locImoveis.pj2back.repository.ContratoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CobrancaServiceTest {

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private CobrancaMapper cobrancaMapper;

    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private CobrancaService cobrancaService;

    @Test
    void geraUmaParcelaPorMesEReutilizaParcelasAoAbrirNovamente() {
        Integer contratoId = 32;
        Usuario locador = Usuario.builder().idUsuario(15).tipoUsuario(TipoUsuario.LOCADOR).build();
        Contrato contrato = Contrato.builder()
                .idContrato(contratoId)
                .dataInicio(LocalDate.of(2026, 1, 1))
                .dataFim(LocalDate.of(2026, 3, 31))
                .diaVencimento(31)
                .valorAcordado(new BigDecimal("1000.00"))
                .imovel(Imovel.builder().locador(locador).build())
                .build();
        List<Cobranca> persistidas = new ArrayList<>();

        when(contratoRepository.findById(contratoId)).thenReturn(Optional.of(contrato));
        when(cobrancaRepository.findByContratoIdContratoOrderByDataVencimentoAsc(contratoId))
                .thenAnswer(invocacao -> List.copyOf(persistidas));
        doAnswer(invocacao -> {
            Iterable<Cobranca> novas = invocacao.getArgument(0);
            for (Cobranca cobranca : novas) {
                cobranca.setIdCobranca(persistidas.size() + 1);
                persistidas.add(cobranca);
            }
            return List.copyOf(persistidas);
        }).when(cobrancaRepository).saveAll(anyIterable());
        when(cobrancaMapper.toDto(any(Cobranca.class))).thenAnswer(invocacao -> {
            Cobranca cobranca = invocacao.getArgument(0);
            return new CobrancaResponseDTO(
                    cobranca.getIdCobranca(),
                    cobranca.getMesReferencia(),
                    cobranca.getDataVencimento(),
                    cobranca.getDataPagamento(),
                    cobranca.getValor(),
                    cobranca.getCobrancaStatus()
            );
        });

        List<CobrancaResponseDTO> primeiraConsulta = cobrancaService.gerarParcelas(contratoId, locador);
        List<CobrancaResponseDTO> segundaConsulta = cobrancaService.gerarParcelas(contratoId, locador);

        assertThat(primeiraConsulta).hasSize(3);
        assertThat(primeiraConsulta).extracting(CobrancaResponseDTO::dataVencimento)
                .containsExactly(
                        LocalDate.of(2026, 1, 31),
                        LocalDate.of(2026, 2, 28),
                        LocalDate.of(2026, 3, 31)
                );
        assertThat(primeiraConsulta).extracting(CobrancaResponseDTO::valor)
                .containsOnly(new BigDecimal("1000.00"));
        assertThat(segundaConsulta).hasSize(3);
        verify(cobrancaRepository, times(1)).saveAll(anyIterable());
    }
}