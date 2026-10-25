package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.imovel.ImovelRequestDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelResponseDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelUpdateDTO;
import com.locImoveis.pj2back.entity.Imovel;
import com.locImoveis.pj2back.entity.enums.OcupacaoStatus;
import com.locImoveis.pj2back.mapper.ImovelMapper;
import com.locImoveis.pj2back.service.ImovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ImovelResponseDTO> criar(@RequestBody ImovelRequestDTO imovelDTO){
        ImovelResponseDTO response = imovelService.criarImovel(imovelDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> buscarPorID(@PathVariable Integer id){
        return ResponseEntity.ok(imovelService.buscarPorId(id));
    }

    @GetMapping("/locador/{locadorId}")
    public ResponseEntity<List<ImovelResponseDTO>> listarPorLocador(@PathVariable Integer locadorId){
        return ResponseEntity.ok(imovelService.buscarPorLocador(locadorId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ImovelResponseDTO>> listarPorStatus(@PathVariable OcupacaoStatus status){
        return ResponseEntity.ok(imovelService.listarPorStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> atualizar(@PathVariable Integer id, @RequestBody ImovelUpdateDTO imovelDTO){
        return ResponseEntity.ok(imovelService.atualizarImovel(id,imovelDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        imovelService.deletarImovel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImovelResponseDTO> uploadFoto(
            @PathVariable Integer id,
            @RequestParam("foto") MultipartFile file) {
        Imovel imovelComFoto = imovelService.uploadFoto(id, file);
        return ResponseEntity.ok(imovelMapper.toDto(imovelComFoto));
    }
}
