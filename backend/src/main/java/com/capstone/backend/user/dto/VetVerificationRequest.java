package com.capstone.backend.user.dto;

public record VetVerificationRequest(
        String licenseNumber,
        String proofImageUrl
) {}
