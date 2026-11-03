package com.locImoveis.pj2back.service;

import com.locImoveis.pj2back.dto.usuario.UsuarioRequestDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioResponseDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioUpdateDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.ContaStatus;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.mapper.UsuarioMapper;
import com.locImoveis.pj2back.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO usuarioDTO) {
        validarUsuarioUnico(usuarioDTO);

        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        String senhaFinal = (usuarioDTO.senha() != null && !usuarioDTO.senha().isBlank())
                ? usuarioDTO.senha()
                : java.util.UUID.randomUUID().toString();

        usuario.setSenha(passwordEncoder.encode(senhaFinal));
        usuario.setContaStatus(ContaStatus.ATIVA);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(usuarioSalvo);
    }

    private UsuarioResponseDTO buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Usuario não encontrado!"));
        return usuarioMapper.toDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO criarLocatario(UsuarioRequestDTO usuarioDTO, Usuario locador) {
        exigirLocador(locador);
        if (usuarioDTO.tipoUsuario() != TipoUsuario.LOCATARIO) {
            throw new IllegalArgumentException("Somente locatários podem ser cadastrados nesta operação.");
        }
        validarUsuarioUnico(usuarioDTO);

        Usuario locatario = usuarioMapper.toEntity(usuarioDTO);
        String senhaFinal = (usuarioDTO.senha() != null && !usuarioDTO.senha().isBlank())
                ? usuarioDTO.senha()
                : java.util.UUID.randomUUID().toString();
        locatario.setSenha(passwordEncoder.encode(senhaFinal));
        locatario.setContaStatus(ContaStatus.ATIVA);
        Usuario locatarioSalvo = usuarioRepository.save(locatario);
        Usuario locadorGerenciado = usuarioRepository.findById(locador.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Proprietário não encontrado!"));
        locadorGerenciado.getLocatarios().add(locatarioSalvo);
        usuarioRepository.saveAndFlush(locadorGerenciado);
        return usuarioMapper.toDTO(locatarioSalvo);
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        return usuarioMapper.toDTO(usuario);
    }

    public UsuarioResponseDTO buscarPorId(Integer id, Usuario locador) {
        exigirLocador(locador);
        if (!locatarioPertenceAoLocador(id, locador.getIdUsuario())) {
            throw new IllegalArgumentException("Usuário não encontrado!");
        }
        return buscarPorId(id);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Integer id, UsuarioUpdateDTO usuarioDTO, Usuario locador) {
        exigirLocador(locador);
        if (!locatarioPertenceAoLocador(id, locador.getIdUsuario())) {
            throw new IllegalArgumentException("Usuário não encontrado!");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        usuarioMapper.updateEntityFromDto(usuarioDTO, usuario);
        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> listarPorTipo(TipoUsuario tipoUsuario, Usuario locador) {
        exigirLocador(locador);
        if (tipoUsuario != TipoUsuario.LOCATARIO) {
            throw new AccessDeniedException("Não é permitido listar usuários deste tipo.");
        }
        Map<Integer, UsuarioResponseDTO> locatarios = new LinkedHashMap<>();
        usuarioRepository.findLocatariosVinculados(tipoUsuario, locador.getIdUsuario())
                .forEach(usuario -> locatarios.put(usuario.getIdUsuario(), usuarioMapper.toDTO(usuario)));
        usuarioRepository.findLocatariosComContrato(tipoUsuario, locador.getIdUsuario())
                .forEach(usuario -> locatarios.put(usuario.getIdUsuario(), usuarioMapper.toDTO(usuario)));
        return List.copyOf(locatarios.values());
    }

    @Transactional
    public void inativar(Integer id, Usuario locador) {
        exigirLocador(locador);
        if (!locatarioPertenceAoLocador(id, locador.getIdUsuario())) {
            throw new IllegalArgumentException("Usuário não encontrado!");
        }
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Usuario não encontrado!"));
        usuario.setContaStatus(ContaStatus.INATIVA);
        usuarioRepository.save(usuario);
    }

    private boolean locatarioPertenceAoLocador(Integer locatarioId, Integer locadorId) {
        return usuarioRepository.pertenceAoLocador(locatarioId, locadorId, TipoUsuario.LOCATARIO);
    }

    private void exigirLocador(Usuario usuario) {
        if (usuario == null || usuario.getTipoUsuario() != TipoUsuario.LOCADOR) {
            throw new AccessDeniedException("Operação disponível somente para proprietários.");
        }
    }

    private void validarUsuarioUnico(UsuarioRequestDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new IllegalArgumentException("Email já existente!");
        }
        if (usuarioRepository.existsByCpf(usuarioDTO.cpf())) {
            throw new IllegalArgumentException("Cpf já existente!");
        }
    }

}
