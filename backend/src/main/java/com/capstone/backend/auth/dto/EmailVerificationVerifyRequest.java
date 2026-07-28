package com.capstone.backend.auth.dto;

public record EmailVerificationVerifyRequest(
        String email,
        String code
) {
}
