package com.novamarket.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Devrait s'authentifier avec succès et recevoir un jeton JWT Bearer")
    void login_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        String loginJson = """
                {
                    "email": "admin@novamarket.com",
                    "password": "Admin123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("admin@novamarket.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @DisplayName("Devrait refuser la connexion avec de mauvais identifiants")
    void login_WithInvalidCredentials_ShouldFail() throws Exception {
        String badLoginJson = """
                {
                    "email": "admin@novamarket.com",
                    "password": "MauvaisMotDePasse!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLoginJson))
                .andExpect(status().isUnauthorized());
    }
}