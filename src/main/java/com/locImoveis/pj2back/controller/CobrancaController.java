package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.cobranca.CobrancaRequestDTO;
import com.locImoveis.pj2back.dto.cobranca.CobrancaResponseDTO;
import com.locImoveis.pj2back.service.CobrancaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cobrancas")
public class CobrancaController {

    private final CobrancaService cobrancaService;

    @PostMapping
    public ResponseEntity<CobrancaResponseDTO> criar(@RequestBody CobrancaRequestDTO cobrancaDTO){
        CobrancaResponseDTO response = cobrancaService.criarCobrança(cobrancaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<CobrancaResponseDTO> registrarPagamento(@PathVariable Integer id){
        return ResponseEntity.ok(cobrancaService.registrarPagamento(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscarPorId(@PathVariable Integer id){
        return ResponseEntity.ok(cobrancaService.buscarPorId(id));
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<CobrancaResponseDTO>> listarPorContrato(@PathVariable Integer contratoId){
        return ResponseEntity.ok(cobrancaService.listarPorContrato(contratoId));
    }

}
