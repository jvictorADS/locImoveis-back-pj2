package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.usuario.UsuarioRequestDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioResponseDTO;
import com.locImoveis.pj2back.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody UsuarioRequestDTO usuarioDTO){
        UsuarioResponseDTO responseDTO = usuarioService.criarUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id){
        UsuarioResponseDTO responseDTO = usuarioService.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }


}
