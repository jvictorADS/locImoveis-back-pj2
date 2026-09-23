package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.auth.AutenticacaoRequestDTO;
import com.locImoveis.pj2back.dto.auth.LoginResponseDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioRequestDTO;
import com.locImoveis.pj2back.dto.usuario.UsuarioResponseDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.service.TokenService;
import com.locImoveis.pj2back.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody AutenticacaoRequestDTO data) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        Authentication authentication = authenticationManager.authenticate(usernamePassword);
        Usuario usuario = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(new LoginResponseDTO(tokenService.generateToken(usuario)));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody UsuarioRequestDTO data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarUsuario(data));
    }
}
