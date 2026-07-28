package com.capstone.backend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
