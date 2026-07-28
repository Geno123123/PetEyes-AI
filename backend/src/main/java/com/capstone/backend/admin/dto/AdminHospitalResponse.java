package com.capstone.backend.admin.dto;

import com.capstone.backend.entity.Hospital;

public record AdminHospitalResponse(
        Long hospitalId,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        boolean parkingAvailable,
        boolean nightCare
) {
    public static AdminHospitalResponse from(Hospital h) {
        return new AdminHospitalResponse(
                h.getHospitalId(),
                h.getName(),
                h.getAddress(),
                h.getPhoneNumber(),
                h.getLatitude(),
                h.getLongitude(),
                h.isParkingAvailable(),
                h.isNightCare()
        );
    }
}
