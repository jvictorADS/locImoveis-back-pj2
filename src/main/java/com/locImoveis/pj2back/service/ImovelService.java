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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImovelService {

    private final ImovelRepository imovelRepository;
    private final ImovelMapper imovelMapper;


    @Transactional
    public ImovelResponseDTO criarImovel(ImovelRequestDTO imovelDTO, Usuario locador) {
        if (locador == null || locador.getTipoUsuario() != TipoUsuario.LOCADOR) {
            throw new AccessDeniedException("Somente proprietários podem cadastrar imóveis.");
        }

        Imovel imovel = imovelMapper.toEntity(imovelDTO);
        imovel.setLocador(locador);
        imovel.setOcupacaoStatus(OcupacaoStatus.DISPONIVEL);

        Imovel imovelSalvo = imovelRepository.save(imovel);

        return imovelMapper.toDto(imovelSalvo);
    }

    @Transactional(readOnly = true)
    public ImovelResponseDTO buscarPorId(Integer id, Usuario locador) {
        Imovel imovel = buscarDoLocador(id, locador);
        return imovelMapper.toDto(imovel);
    }

    @Transactional(readOnly = true)
    public List<ImovelResponseDTO> buscarPorLocador(Usuario locador) {
        exigirLocador(locador);
        return imovelRepository.findByLocadorIdUsuario(locador.getIdUsuario())
                .stream().map(imovelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ImovelResponseDTO> listarPorStatus(OcupacaoStatus ocupacaoStatus, Usuario locador) {
        exigirLocador(locador);
        return imovelRepository.findByLocadorIdUsuarioAndOcupacaoStatus(locador.getIdUsuario(), ocupacaoStatus)
                .stream().map(imovelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ImovelResponseDTO atualizarImovel(Integer id, ImovelUpdateDTO imovelDTO, Usuario locador) {
        Imovel imovel = buscarDoLocador(id, locador);
        imovelMapper.updateEntityFromDto(imovelDTO, imovel);
        return imovelMapper.toDto(imovelRepository.save(imovel));
    }

    public void deletarImovel(Integer id, Usuario locador) {
        imovelRepository.delete(buscarDoLocador(id, locador));
    }

    // upload para foto do imóvel
    public Imovel uploadFoto(Integer id, MultipartFile file, Usuario locador) {
        Imovel imovel = buscarDoLocador(id, locador);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("O ficheiro enviado está vazio.");
        }

        try {
            // Cria a pasta 'uploads' caso não exista
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);}

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Define a URL pública de acesso à imagem
            String fotoUrl = "http://localhost:8081/uploads/" + fileName;
            imovel.setFotoUrl(fotoUrl);

            return imovelRepository.save(imovel);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao guardar a imagem: " + e.getMessage());
        }
    }

    private Imovel buscarDoLocador(Integer id, Usuario locador) {
        exigirLocador(locador);
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imóvel não encontrado."));
        if (!imovel.getLocador().getIdUsuario().equals(locador.getIdUsuario())) {
            throw new EntityNotFoundException("Imóvel não encontrado.");
        }
        return imovel;
    }

    private void exigirLocador(Usuario locador) {
        if (locador == null || locador.getTipoUsuario() != TipoUsuario.LOCADOR) {
            throw new AccessDeniedException("Operação disponível somente para proprietários.");
        }
    }

}
