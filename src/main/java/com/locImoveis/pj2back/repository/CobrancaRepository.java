package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.Cobranca;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CobrancaRepository extends JpaRepository<Cobranca, Integer> {
    List<Cobranca> findByContratoIdContrato(Integer contratoId);
    List<Cobranca> findByCobrancaStatus(CobrancaStatus cobrancaStatus);
}
