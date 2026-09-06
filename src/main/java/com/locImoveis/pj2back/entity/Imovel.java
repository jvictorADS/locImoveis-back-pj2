package com.locImoveis.pj2back.entity;

import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "imoveis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Imovel")
    private Long idImovel;

    @Column(name = "endereco", nullable = false)
    private String endereco;

    @Column(name = "valor_aluguel", nullable = false)
    private BigDecimal valorAluguel;

    @Column(name = "valor_iptu")
    private BigDecimal valorIPTU;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocupacao_status", nullable = false)
    private OcupacaoStatus ocupacaoStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locador_id", nullable = false)
    private Usuario locador;
}
