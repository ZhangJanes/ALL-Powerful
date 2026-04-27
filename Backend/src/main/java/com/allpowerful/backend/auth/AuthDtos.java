package com.allpowerful.backend.auth;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, Long userId, String username, String displayName) {}
    public record MeResponse(Long id, String username, String displayName) {}
}
