package com.capstone.backend.hospital.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.hospital.dto.NearbyHospitalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

public interface HospitalControllerDocs {

    @Operation(
            summary = "주변 동물병원 조회",
            description = "사용자 좌표 기준 반경(km) 내 동물병원을 조회합니다. 기본은 거리순이며 sortByReviewCount=true면 리뷰 많은 순(동률 시 거리순)으로 정렬합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = NearbyHospitalResponse.class)),
                            examples = @ExampleObject(
                                    name = "nearby-hospitals-success",
                                    value = """
                                            [
                                              {
                                                "hospitalId": 12,
                                                "name": "경희궁 바른 동물병원",
                                                "address": "서울특별시 종로구 송월길 99",
                                                "phoneNumber": "02-1234-5678",
                                                "latitude": 37.5668,
                                                "longitude": 126.9784,
                                                "distanceKm": 0.42,
                                                "averageRating": 4.6,
                                                "naverMapUrl": "https://map.naver.com/p/search/..."
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 좌표 또는 파라미터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    List<NearbyHospitalResponse> getNearbyHospitals(
            @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "사용자 위도", example = "37.5665") Double latitude,
            @Parameter(description = "사용자 경도", example = "126.9780") Double longitude,
            @Parameter(description = "검색 반경(km), 기본값 5", example = "5") Double radiusKm,
            @Parameter(description = "최대 조회 개수, 기본값 20", example = "20") Integer limit,
            @Parameter(description = "true면 운영시간 기반 야간진료 병원만 필터링", example = "true") boolean night,
            @Parameter(description = "true면 리뷰 많은 순 정렬(동률 시 거리순)", example = "true") boolean sortByReviewCount
    );
}
