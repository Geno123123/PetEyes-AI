package com.capstone.backend.admin.dto;

public record AdminVetVerificationUpdateRequest(
        String status,
        String reviewNote
) {}
