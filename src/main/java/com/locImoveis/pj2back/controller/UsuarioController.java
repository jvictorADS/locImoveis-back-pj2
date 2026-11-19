package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.usuario.UsuarioRequestDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioResponseDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioUpdateDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.TipoUsuario;
import com.locImoveis.pj2back.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO,
            @AuthenticationPrincipal Usuario locador
    ){
        UsuarioResponseDTO responseDTO = usuarioService.criarLocatario(usuarioDTO, locador);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal Usuario locador
    ){
        UsuarioResponseDTO responseDTO = usuarioService.buscarPorId(id, locador);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorTipo(
            @PathVariable TipoUsuario tipo,
            @AuthenticationPrincipal Usuario locador
    ) {
        return ResponseEntity.ok(usuarioService.listarPorTipo(tipo, locador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateDTO usuarioDTO,
            @AuthenticationPrincipal Usuario locador
    ){
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, usuarioDTO, locador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Integer id, @AuthenticationPrincipal Usuario locador) {
        usuarioService.inativar(id, locador);
        return ResponseEntity.noContent().build();
    }


}
