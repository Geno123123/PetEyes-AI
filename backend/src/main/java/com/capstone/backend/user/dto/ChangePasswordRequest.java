package com.capstone.backend.user.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
