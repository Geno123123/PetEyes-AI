package com.capstone.backend.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "네이버 검색 기반 주변 동물병원 조회 결과")
public record NaverNearbyHospitalResponse(
        @Schema(description = "DB 병원 ID (리뷰 작성 시 사용)", example = "42")
        Long hospitalId,
        @Schema(description = "병원명", example = "OO동물병원")
        String name,
        @Schema(description = "카테고리", example = "동물병원>종합동물병원")
        String category,
        @Schema(description = "지번 주소", example = "서울특별시 중구 명동 ...")
        String address,
        @Schema(description = "도로명 주소", example = "서울특별시 중구 세종대로 110")
        String roadAddress,
        @Schema(description = "전화번호", example = "02-1234-5678")
        String telephone,
        @Schema(description = "위도", example = "37.5668")
        Double latitude,
        @Schema(description = "경도", example = "126.9784")
        Double longitude,
        @Schema(description = "사용자 좌표로부터의 거리(km)", example = "0.42")
        Double distanceKm,
        @Schema(description = "병원 외부 링크")
        String link,
        @Schema(description = "Naver 지도 딥링크")
        String naverMapUrl
) {
}
