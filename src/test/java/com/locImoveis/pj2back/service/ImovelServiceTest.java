package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.mapper.ImovelMapper;
import com.locImoveis.pj2back.repository.ImovelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImovelServiceTest {

    @Mock
    private ImovelRepository imovelRepository;

    @Mock
    private ImovelMapper imovelMapper;

    @InjectMocks
    private ImovelService imovelService;

    @Test
    void naoPermiteConsultarImovelDeOutroProprietario() {
        Usuario proprietarioAutenticado = Usuario.builder()
                .idUsuario(10)
                .tipoUsuario(TipoUsuario.LOCADOR)
                .build();
        Usuario outroProprietario = Usuario.builder()
                .idUsuario(20)
                .tipoUsuario(TipoUsuario.LOCADOR)
                .build();
        Imovel imovelDeOutraConta = Imovel.builder()
                .idImovel(30)
                .locador(outroProprietario)
                .build();
        when(imovelRepository.findById(30)).thenReturn(Optional.of(imovelDeOutraConta));

        assertThatThrownBy(() -> imovelService.buscarPorId(30, proprietarioAutenticado))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Imóvel não encontrado.");

        verify(imovelRepository).findById(30);
    }
}
