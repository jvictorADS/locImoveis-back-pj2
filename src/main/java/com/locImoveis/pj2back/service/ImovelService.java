package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.imovel.ImovelRequestDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelResponseDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelUpdateDTO;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.mapper.ImovelMapper;
import com.locImoveis.pj2back.repository.ImovelRepository;
import com.locImoveis.pj2back.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImovelService {

    private final ImovelRepository imovelRepository;
    private final ImovelMapper imovelMapper;
    private final UsuarioRepository usuarioRepository;


    @Transactional
    public ImovelResponseDTO criarImovel(ImovelRequestDTO imovelDTO) {
        Usuario locador = usuarioRepository.findById(imovelDTO.locadorId())
                .orElseThrow(() -> new IllegalArgumentException("Locador não encontrado!"));

        if(locador.getTipoUsuario() == TipoUsuario.LOCATARIO) {
            throw new IllegalArgumentException("Um usuário do tipo LOCATÁRIO não pode ser designado como proprietário de um imóvel.");
        }

        Imovel imovel = imovelMapper.toEntity(imovelDTO);
        imovel.setLocador(locador);
        imovel.setOcupacaoStatus(OcupacaoStatus.DISPONIVEL);

        Imovel imovelSalvo = imovelRepository.save(imovel);

        return imovelMapper.toDto(imovelSalvo);
    }

    public ImovelResponseDTO buscarPorId(Integer id) {
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imovel não encontrado!"));
        return imovelMapper.toDto(imovel);
    }

    public List<ImovelResponseDTO> buscarPorLocador(Integer locadorId) {
        return imovelRepository.findByLocadorIdUsuario(locadorId)
                .stream().map(imovelMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ImovelResponseDTO> listarPorStatus(OcupacaoStatus ocupacaoStatus) {
        return imovelRepository.findByOcupacaoStatus(ocupacaoStatus)
                .stream().map(imovelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ImovelResponseDTO atualizarImovel(Integer id, ImovelUpdateDTO imovelDTO) {
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado!"));
        imovelMapper.updateEntityFromDto(imovelDTO, imovel);
        return imovelMapper.toDto(imovelRepository.save(imovel));
    }

}
