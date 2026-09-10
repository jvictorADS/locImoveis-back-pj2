package com.locImoveis.pj2back.entity;

import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cobrancas")
@Data @NoArgsConstructor
@AllArgsConstructor @Builder
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cobranca")
    private Integer idCobranca;

    @Column(name = "mes_referencia", nullable = false)
    private String mesReferencia;

    // data limite para o pagamento
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "cobranca_status", nullable = false)
    private CobrancaStatus cobrancaStatus;

    @Column(name = "link_arquivo_pdf")
    private String linkArquivoPdf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

}
