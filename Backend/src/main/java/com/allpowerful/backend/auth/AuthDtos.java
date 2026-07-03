package com.allpowerful.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 6, max = 64) String password,
            @Size(max = 64) String displayName
    ) {}

    public record LoginResponse(String token, Long userId, String username, String displayName) {}

    public record MeResponse(Long id, String username, String displayName) {}
}
