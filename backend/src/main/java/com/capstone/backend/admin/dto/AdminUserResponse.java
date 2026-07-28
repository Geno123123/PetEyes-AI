package com.capstone.backend.admin.dto;

import com.capstone.backend.entity.User;

public record AdminUserResponse(
        Long id,
        String email,
        String name,
        String phoneNumber,
        String role,
        String vetVerificationStatus,
        String vetLicenseNumber,
        String vetProofImageUrl
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getVetVerificationStatus().name(),
                user.getVetLicenseNumber(),
                user.getVetProofImageUrl()
        );
    }
}
