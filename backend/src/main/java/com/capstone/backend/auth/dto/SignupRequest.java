package com.capstone.backend.auth.dto;

import com.capstone.backend.entity.type.Role;

public record SignupRequest(
        String email,
        String password,
        String name,
        String nickname,
        String phoneNumber,
        Role role
) {
}
