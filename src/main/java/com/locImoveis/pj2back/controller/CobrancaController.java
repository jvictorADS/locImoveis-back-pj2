package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.cobranca.CobrancaRequestDTO;
import com.locImoveis.pj2back.dto.cobranca.CobrancaResponseDTO;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.service.CobrancaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cobrancas")
public class CobrancaController {

    private final CobrancaService cobrancaService;

    @PostMapping
    public ResponseEntity<CobrancaResponseDTO> criar(
            @RequestBody CobrancaRequestDTO cobrancaDTO,
            @AuthenticationPrincipal Usuario locador
    ){
        CobrancaResponseDTO response = cobrancaService.criarCobrança(cobrancaDTO, locador);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/pagar")
        public ResponseEntity<CobrancaResponseDTO> registrarPagamento(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
        ) {
        return ResponseEntity.ok(cobrancaService.registrarPagamento(id, locador));
    }

    @PutMapping("/{id}/pendente")
    public ResponseEntity<CobrancaResponseDTO> marcarPendente(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(cobrancaService.marcarPendente(id, locador));
    }

    @PostMapping("/contrato/{contratoId}/parcelas/gerar")
    public ResponseEntity<List<CobrancaResponseDTO>> gerarParcelas(
            @PathVariable Integer contratoId,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(cobrancaService.gerarParcelas(contratoId, locador));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(cobrancaService.buscarPorId(id, locador));
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<CobrancaResponseDTO>> listarPorContrato(
            @PathVariable Integer contratoId,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(cobrancaService.listarPorContrato(contratoId, locador));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CobrancaResponseDTO>> listarPorStatus(
            @PathVariable CobrancaStatus status,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(cobrancaService.listarPorStatus(status, locador));
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<CobrancaResponseDTO> cancelar(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ){
        cobrancaService.cancelarCobranca(id, locador);
        return ResponseEntity.noContent().build();
    }

}
