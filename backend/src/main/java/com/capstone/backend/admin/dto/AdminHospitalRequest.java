package com.capstone.backend.admin.dto;

public record AdminHospitalRequest(
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        boolean parkingAvailable,
        boolean nightCare
) {}
