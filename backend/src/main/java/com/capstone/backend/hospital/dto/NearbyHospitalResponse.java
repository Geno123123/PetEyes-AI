package com.capstone.backend.hospital.dto;

import com.capstone.backend.entity.Hospital;
import com.capstone.backend.hospital.util.NaverMapUrls;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주변 병원 조회 결과")
public record NearbyHospitalResponse(
        @Schema(description = "병원 ID", example = "12")
        Long hospitalId,
        @Schema(description = "병원명", example = "OO동물병원")
        String name,
        @Schema(description = "병원 주소", example = "서울특별시 종로구 송월길 99")
        String address,
        @Schema(description = "병원 전화번호", example = "02-1234-5678")
        String phoneNumber,
        @Schema(description = "병원 위도", example = "37.5668")
        Double latitude,
        @Schema(description = "병원 경도", example = "126.9784")
        Double longitude,
        @Schema(description = "사용자 현재 위치로부터의 거리(km)", example = "0.42")
        Double distanceKm,
        @Schema(description = "병원 평균 평점", example = "4.6")
        Double averageRating,
        @Schema(description = "Naver 지도 딥링크")
        String naverMapUrl
) {
    public static NearbyHospitalResponse from(Hospital hospital, double distanceKm, double averageRating) {
        return new NearbyHospitalResponse(
                hospital.getHospitalId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhoneNumber(),
                hospital.getLatitude(),
                hospital.getLongitude(),
                distanceKm,
                averageRating,
                NaverMapUrls.deeplink(hospital.getName(), hospital.getLatitude(), hospital.getLongitude())
        );
    }
}
