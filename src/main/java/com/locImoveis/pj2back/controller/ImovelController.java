package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.imovel.ImovelRequestDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelResponseDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelUpdateDTO;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.mapper.ImovelMapper;
import com.locImoveis.pj2back.service.ImovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/imoveis")
public class ImovelController {

    private final ImovelService imovelService;
    private final ImovelMapper imovelMapper;

    @PostMapping
    public ResponseEntity<ImovelResponseDTO> criar(
            @RequestBody ImovelRequestDTO imovelDTO,
            @AuthenticationPrincipal Usuario locador
    ){
        ImovelResponseDTO response = imovelService.criarImovel(imovelDTO, locador);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> buscarPorID(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(imovelService.buscarPorId(id, locador));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ImovelResponseDTO>> listarDoLocador(@AuthenticationPrincipal Usuario locador) {
        return ResponseEntity.ok(imovelService.buscarPorLocador(locador));
    }

    @GetMapping("/locador/{locadorId}")
    public ResponseEntity<List<ImovelResponseDTO>> listarPorLocador(
            @PathVariable Integer locadorId,
            @AuthenticationPrincipal Usuario locador
    ){
        if (locador == null || !locadorId.equals(locador.getIdUsuario())) {
            throw new AccessDeniedException("Não é permitido consultar imóveis de outro proprietário.");
        }
        return ResponseEntity.ok(imovelService.buscarPorLocador(locador));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ImovelResponseDTO>> listarPorStatus(
            @PathVariable OcupacaoStatus status,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(imovelService.listarPorStatus(status, locador));
    }

    @GetMapping("/locatario/{locatarioId}")
    public ResponseEntity<List<ImovelResponseDTO>> listarPorLocatario(
            @PathVariable Integer locatarioId,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(imovelService.buscarPorLocatario(locatarioId, locador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestBody ImovelUpdateDTO imovelDTO,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(imovelService.atualizarImovel(id, imovelDTO, locador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id, @AuthenticationPrincipal Usuario locador) {
        imovelService.deletarImovel(id, locador);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImovelResponseDTO> uploadFoto(
            @PathVariable Integer id,
            @RequestParam("foto") MultipartFile file,
            @AuthenticationPrincipal Usuario locador) {
        Imovel imovelComFoto = imovelService.uploadFoto(id, file, locador);
        return ResponseEntity.ok(imovelMapper.toDto(imovelComFoto));
    }
}
