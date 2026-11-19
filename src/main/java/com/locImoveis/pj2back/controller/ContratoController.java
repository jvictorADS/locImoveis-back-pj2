package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.contrato.ContratoRequestDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoResponseDTO;
import com.locImoveis.pj2back.dto.contrato.ContratoUpdateDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    public ResponseEntity<ContratoResponseDTO> criar(
            @RequestBody ContratoRequestDTO contratoDTO,
            @AuthenticationPrincipal Usuario locador
    ) {
        ContratoResponseDTO response = contratoService.criarContrato(contratoDTO, locador);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponseDTO> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(contratoService.buscarPorId(id, locador));
    }

    @GetMapping("/imovel/{imovelId}")
    public ResponseEntity<List<ContratoResponseDTO>> listarPorImovel(
            @PathVariable Integer imovelId,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(contratoService.listarPorImovel(imovelId, locador));
    }

    @GetMapping("/locatario/{locatarioId}")
    public ResponseEntity<List<ContratoResponseDTO>> listarPorLocatario(
            @PathVariable Integer locatarioId,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(contratoService.listarPorLocatario(locatarioId, locador));
    }

    @GetMapping
    public ResponseEntity<List<ContratoResponseDTO>> listarTodos(@AuthenticationPrincipal Usuario locador) {
        return ResponseEntity.ok(contratoService.listarTodos(locador));
    }

    @DeleteMapping("/{id}/rescindir")
    public ResponseEntity<ContratoResponseDTO> rescindir(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(contratoService.rescindirContrato(id, locador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratoResponseDTO> atualizarContrato(
            @PathVariable Integer id,
            @RequestBody ContratoUpdateDTO dto,
            @AuthenticationPrincipal Usuario locador) {
        ContratoResponseDTO responseDTO = contratoService.atualizarContrato(id, dto, locador);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(value = "/{id}/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContratoResponseDTO> uploadDocumento(
            @PathVariable Integer id,
            @RequestParam("documento") MultipartFile documento,
            @AuthenticationPrincipal Usuario locador) {

        ContratoResponseDTO response = contratoService.uploadDocumento(id, documento, locador);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<byte[]> obterDocumento(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ) {
        byte[] pdfBytes = contratoService.obterDocumento(id, locador);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"contrato_" + id + ".pdf\"")
                .body(pdfBytes);
    }

}
