package com.novamarket.controller;

import com.novamarket.dto.AuthRequest;
import com.novamarket.dto.AuthResponse;
import com.novamarket.dto.RegisterRequest;
import com.novamarket.security.CustomUserDetails;
import com.novamarket.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'inscription, connexion et récupération de profil (JWT)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscription client", description = "Crée un nouveau compte client, hache le mot de passe avec BCrypt et émet un jeton JWT.")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur (Admin ou Client) et retourne un jeton JWT Bearer.")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Profil de l'utilisateur connecté", description = "Retourne les informations de l'utilisateur extrait du jeton JWT fourni.")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Map.of(
                "id", userDetails.getId(),
                "email", userDetails.getUsername(),
                "firstName", userDetails.getFirstName(),
                "lastName", userDetails.getLastName(),
                "roles", userDetails.getAuthorities()
        );
    }
}