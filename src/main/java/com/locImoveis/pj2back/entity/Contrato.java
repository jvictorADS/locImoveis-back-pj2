package com.locImoveis.pj2back.entity;

import com.locImoveis.pj2back.entity.enums.ContratoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contratos")
@Data @NoArgsConstructor
@AllArgsConstructor @Builder
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Integer idContrato;

    // início do contrato
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    // fim do contrato
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    // dia do pagamento
    @Column(name = "dia_vencimento", nullable = false)
    private Integer diaVencimento;

    // caso aja acordo entre locador e locatario em relação a um desconto no aluguel
    @Column(name = "valor_acordado", nullable = false)
    private BigDecimal valorAcordado;

    // controle de reajuste anual
    @Column(name = "data_proximo_reajuste")
    private LocalDate dataProximoReajuste;

    // indice de aumento (IPCA, IGPM, Fixo, Nenhum)
    @Column(name = "indice_reajuste", length = 20)
    private String indiceReajuste;

    @Enumerated(EnumType.STRING)
    @Column(name = "contrato_status", nullable = false)
    private ContratoStatus contratoStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imovel_id", nullable = false)
    private Imovel imovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatario_id", nullable = false)
    private Usuario locatario;
}
