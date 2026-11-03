package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.PedidoManutencao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoManutencaoRepository extends JpaRepository<PedidoManutencao, Integer> {
    List<PedidoManutencao> findByContrato_Imovel_Locador_IdUsuarioOrderByDataAberturaDesc(Integer locadorId);
}