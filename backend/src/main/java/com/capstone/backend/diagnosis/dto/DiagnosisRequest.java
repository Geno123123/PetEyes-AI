package com.capstone.backend.diagnosis.dto;

import com.capstone.backend.entity.type.EyePosition;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "진단 결과 저장 요청 (ML 모델 결과값 전달)")
public record DiagnosisRequest(
        @Schema(description = "진단 이미지 URL (S3 업로드 후 URL)") String imageUrl,
        @Schema(description = "촬영 부위", example = "LEFT") EyePosition eyePosition,
        @Schema(description = "질병 ID") Long diseaseId,
        @Schema(description = "ML 모델 원시 중증도 값", example = "유") String rawSeverity,
        @Schema(description = "ML 모델 정확도 (0~100)", example = "92.1") Double confidence
) {}
