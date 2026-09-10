package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.imovel.ImovelRequestDTO;
import com.locImoveis.pj2back.dto.imovel.ImovelResponseDTO;
import com.locImoveis.pj2back.service.ImovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/imoveis")
public class ImovelController {

    private final ImovelService imovelService;

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

}
