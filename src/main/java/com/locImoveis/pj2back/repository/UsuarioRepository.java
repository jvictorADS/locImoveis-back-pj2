package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);
}
