package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    @Query("""
            select distinct locatario
            from Usuario locador
            join locador.locatarios locatario
            where locador.idUsuario = :locadorId
              and locatario.tipoUsuario = :tipo
            """)
    List<Usuario> findLocatariosVinculados(
            @Param("tipo") TipoUsuario tipo,
            @Param("locadorId") Integer locadorId
    );

    @Query("""
            select distinct contrato.locatario
            from Contrato contrato
            where contrato.imovel.locador.idUsuario = :locadorId
              and contrato.locatario.tipoUsuario = :tipo
            """)
    List<Usuario> findLocatariosComContrato(
            @Param("tipo") TipoUsuario tipo,
            @Param("locadorId") Integer locadorId
    );

    @Query("""
            select count(locatario) > 0
            from Usuario locador
            join locador.locatarios locatario
            where locador.idUsuario = :locadorId
              and locatario.idUsuario = :locatarioId
              and locatario.tipoUsuario = :tipo
            """)
    boolean locatarioVinculadoAoLocador(
            @Param("locatarioId") Integer locatarioId,
            @Param("locadorId") Integer locadorId,
            @Param("tipo") TipoUsuario tipo
    );

    @Query("""
            select count(contrato) > 0
            from Contrato contrato
            where contrato.locatario.idUsuario = :locatarioId
              and contrato.imovel.locador.idUsuario = :locadorId
              and contrato.locatario.tipoUsuario = :tipo
            """)
    boolean locatarioPossuiContratoComLocador(
            @Param("locatarioId") Integer locatarioId,
            @Param("locadorId") Integer locadorId,
            @Param("tipo") TipoUsuario tipo
    );

    default boolean pertenceAoLocador(Integer locatarioId, Integer locadorId, TipoUsuario tipo) {
        return locatarioVinculadoAoLocador(locatarioId, locadorId, tipo)
                || locatarioPossuiContratoComLocador(locatarioId, locadorId, tipo);
    }
}
