package com.capstone.backend.user.dto;

public record UpdateUserRequest(
        String name,
        String nickname,
        String profileImageUrl,
        String phoneNumber
) {
}
