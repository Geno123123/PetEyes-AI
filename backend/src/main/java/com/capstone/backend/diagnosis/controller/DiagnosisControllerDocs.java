package com.capstone.backend.diagnosis.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.diagnosis.dto.DiagnosisRequest;
import com.capstone.backend.diagnosis.dto.DiagnosisResponse;
import com.capstone.backend.diagnosis.dto.DiagnosisTrendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Diagnoses", description = "진단 API")
public interface DiagnosisControllerDocs {

    @Operation(summary = "진단 결과 저장", description = "ML 모델 결과를 받아 진단 기록을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "404", description = "반려동물 또는 질병 없음")
    })
    DiagnosisResponse createDiagnosis(
            @Parameter(hidden = true) UserPrincipal principal,
            Long petId,
            DiagnosisRequest request
    );

    @Operation(summary = "진단 목록 조회", description = "반려동물의 진단 기록 전체를 최신순으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<DiagnosisResponse> getDiagnoses(
            @Parameter(hidden = true) UserPrincipal principal,
            Long petId
    );

    @Operation(summary = "진단 상세 조회", description = "특정 진단 결과와 질병 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "진단 없음")
    })
    DiagnosisResponse getDiagnosis(
            @Parameter(hidden = true) UserPrincipal principal,
            Long petId,
            Long diagnosisId
    );

    @Operation(summary = "진단 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    void deleteDiagnosis(
            @Parameter(hidden = true) UserPrincipal principal,
            Long petId,
            Long diagnosisId
    );

    @Operation(summary = "진단 추이 조회", description = "반려동물의 진단 기록을 시간순으로 정렬하여 추이 데이터를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    DiagnosisTrendResponse getTrend(
            @Parameter(hidden = true) UserPrincipal principal,
            Long petId
    );
}
