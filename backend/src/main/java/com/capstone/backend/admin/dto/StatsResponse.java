package com.capstone.backend.admin.dto;

public record StatsResponse(
        long totalUsers,
        long totalHospitals,
        long totalDiagnoses,
        long totalReviews,
        long totalQnaPosts
) {}
