package com.locImoveis.pj2back.repository;

import com.locImoveis.pj2back.entity.Contrato;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {
    List<Contrato> findByLocatarioIdUsuario(Integer locatarioId);
    List<Contrato> findByImovelIdImovel(Integer imovelId);
    List<Contrato> findByImovel_Locador_IdUsuarioOrderByDataInicioDesc(Integer locadorId);
    List<Contrato> findByImovel_Locador_IdUsuarioAndLocatario_IdUsuarioAndContratoStatus(
            Integer locadorId, Integer locatarioId, ContratoStatus contratoStatus);

    Integer imovel(Imovel imovel);
}
