package com.capstone.backend.auth.dto;

public record AuthCodeTokenRequest(
        String code,
        String signupCode,
        String name,
        String nickname,
        String phoneNumber
) {
}
