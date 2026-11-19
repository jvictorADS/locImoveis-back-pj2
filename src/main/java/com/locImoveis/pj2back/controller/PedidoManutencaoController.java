package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.manutencao.ContratoOpcaoDTO;
import com.locImoveis.pj2back.dto.manutencao.ManutencaoRequestDTO;
import com.locImoveis.pj2back.dto.manutencao.ManutencaoResponseDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.service.PedidoManutencaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/manutencoes")
public class PedidoManutencaoController {

    private final PedidoManutencaoService manutencaoService;

    @GetMapping
    public List<ManutencaoResponseDTO> listar(@AuthenticationPrincipal Usuario locador) {
        return manutencaoService.listar(locador);
    }

    @GetMapping("/contratos")
    public List<ContratoOpcaoDTO> listarContratos(@AuthenticationPrincipal Usuario locador) {
        return manutencaoService.listarContratos(locador);
    }

    @GetMapping("/{id}")
    public ManutencaoResponseDTO buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ) {
        return manutencaoService.buscarPorId(id, locador);
    }

    @PostMapping
    public ResponseEntity<ManutencaoResponseDTO> criar(
            @Valid @RequestBody ManutencaoRequestDTO dto,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manutencaoService.criar(dto, locador));
    }

    @PutMapping("/{id}")
    public ManutencaoResponseDTO atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ManutencaoRequestDTO dto,
            @AuthenticationPrincipal Usuario locador
    ) {
        return manutencaoService.atualizar(id, dto, locador);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ) {
        manutencaoService.excluir(id, locador);
        return ResponseEntity.noContent().build();
    }
}