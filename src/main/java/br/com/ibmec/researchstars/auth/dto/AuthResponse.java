package br.com.ibmec.researchstars.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String name,
        String role,
        String status
) {}
