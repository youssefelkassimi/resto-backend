package com.fst.rsi.resto.controller;

import com.fst.rsi.resto.dto.AuthResponseDTO;
import com.fst.rsi.resto.dto.ClientRequestDTO;
import com.fst.rsi.resto.dto.LoginRequestDTO;
import com.fst.rsi.resto.dto.RefreshTokenRequestDTO;
import com.fst.rsi.resto.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API pour l'authentification JWT")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Inscription client", description = "Crée un nouveau compte client et retourne un token JWT")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody ClientRequestDTO registerRequest) {
        AuthResponseDTO response = authenticationService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur et retourne un token JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", description = "Génère un nouveau token JWT à partir d'un refresh token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        AuthResponseDTO response = authenticationService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }
}

