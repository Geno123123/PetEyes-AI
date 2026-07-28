package com.capstone.backend.auth.dto;

public record AuthErrorResponse(
        String errorCode,
        String message
) {
}
