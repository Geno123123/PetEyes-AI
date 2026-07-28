package com.capstone.backend.diagnosis.dto;

import com.capstone.backend.disease.dto.DiseaseResponse;
import com.capstone.backend.entity.Diagnosis;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "진단 결과 응답")
public record DiagnosisResponse(
        @Schema(description = "진단 ID") Long id,
        @Schema(description = "반려동물 ID") Long petId,
        @Schema(description = "진단 이미지 URL") String imageUrl,
        @Schema(description = "촬영 부위") String eyePosition,
        @Schema(description = "질병명") String diseaseName,
        @Schema(description = "원시 중증도 (ML 결과)") String severity,
        @Schema(description = "단순화 중증도", example = "NORMAL / CAUTION / DANGER") String severityLevel,
        @Schema(description = "중증도 라벨", example = "정상 / 관찰 필요 / 진료 권장") String severityLabel,
        @Schema(description = "정확도") Double confidence,
        @Schema(description = "RGB/HSV 기반 점수") Double rgbHsvScore,
        @Schema(description = "진단 일시") LocalDateTime createdAt,
        @Schema(description = "질병 상세 정보") DiseaseResponse disease
) {
    public static DiagnosisResponse from(Diagnosis d) {
        return new DiagnosisResponse(
                d.getId(),
                d.getPet().getId(),
                d.getImageUrl(),
                d.getEyePosition().name(),
                d.getDiseaseName(),
                d.getSeverity(),
                d.toSeverityLevel(),
                d.toSeverityLabel(),
                d.getConfidence(),
                d.getRgbHsvScore(),
                d.getCreatedAt(),
                DiseaseResponse.from(d.getDisease())
        );
    }
}
