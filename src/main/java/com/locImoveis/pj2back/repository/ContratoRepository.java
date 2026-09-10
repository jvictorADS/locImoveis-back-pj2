package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {
    List<Contrato> findByLocatarioIdUsuario(Integer locatarioId);
    List<Contrato> findByImovelIdImovel(Integer imovelId);

    Integer imovel(Imovel imovel);
}
