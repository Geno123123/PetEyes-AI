package com.capstone.backend.user.dto;

import com.capstone.backend.entity.User;

public record UserMeResponse(
        Long id,
        String email,
        String name,
        String nickname,
        String profileImageUrl,
        String phoneNumber,
        String role,
        String vetVerificationStatus
) {

    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getVetVerificationStatus().name()
        );
    }
}
