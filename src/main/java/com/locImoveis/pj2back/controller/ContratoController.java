package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.contrato.ContratoRequestDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoResponseDTO;
import com.locImoveis.pj2back.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    public ResponseEntity<ContratoResponseDTO> criar(@RequestBody ContratoRequestDTO contratoDTO) {
        ContratoResponseDTO response = contratoService.criarContrato(contratoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(contratoService.buscarPorId(id));
    }

    @GetMapping("/imovel/{imovelId}")
    public ResponseEntity<List<ContratoResponseDTO>> listarPorImovel(@PathVariable Integer imovelId) {
        return ResponseEntity.ok(contratoService.listarPorImovel(imovelId));
    }

}
