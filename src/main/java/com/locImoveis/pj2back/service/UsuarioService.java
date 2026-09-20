package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.usuario.UsuarioRequestDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioResponseDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioUpdateDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.ContaStatus;
import com.locImoveis.pj2back.mapper.UsuarioMapper;
import com.locImoveis.pj2back.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO usuarioDTO) {
        if(usuarioRepository.existsByEmail(usuarioDTO.email())){
            throw new IllegalArgumentException("Email já existente!");
        }
        if (usuarioRepository.existsByCpf(usuarioDTO.cpf())) {
            throw new IllegalArgumentException("Cpf já existente!");
        }

        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setContaStatus(ContaStatus.ATIVA);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(usuarioSalvo);
    }

    public UsuarioResponseDTO buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Usuario não encontrado!"));
        return usuarioMapper.toDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Integer id, UsuarioUpdateDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        usuarioMapper.updateEntityFromDto(usuarioDTO, usuario);
        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void inativar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Usuario não encontrado!"));
        usuario.setContaStatus(ContaStatus.INATIVA);
        usuarioRepository.save(usuario);
    }

}
