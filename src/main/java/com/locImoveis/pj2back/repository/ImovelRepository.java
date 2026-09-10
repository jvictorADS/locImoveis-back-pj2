package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Integer> {
    List<Imovel> findByLocadorIdUsuario(Integer locadorId);
}
