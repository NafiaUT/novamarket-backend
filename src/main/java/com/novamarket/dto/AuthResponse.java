package com.novamarket.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        long expiresInMs
) {
    public static AuthResponse of(String token, String email, String firstName, String lastName, List<String> roles, long expiresInMs) {
        return new AuthResponse(token, "Bearer", email, firstName, lastName, roles, expiresInMs);
    }
}