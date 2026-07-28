package com.capstone.backend.disease.controller;

import com.capstone.backend.disease.dto.DiseaseResponse;
import com.capstone.backend.disease.dto.DiseaseSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Diseases", description = "안구 질환 정보 API")
public interface DiseaseControllerDocs {

    @Operation(summary = "질환 목록 조회", description = "등록된 모든 안구 질환 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<DiseaseSummaryResponse> getAllDiseases();

    @Operation(summary = "질환 상세 조회", description = "질환 ID로 상세 정보(증상, 원인, 예방법 등)를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "질환 없음")
    DiseaseResponse getDisease(Long diseaseId);
}
