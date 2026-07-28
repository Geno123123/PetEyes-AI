package com.capstone.backend.disease.dto;

import com.capstone.backend.entity.Disease;
import com.capstone.backend.entity.type.Species;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "질병 상세 응답")
public record DiseaseResponse(
        @Schema(description = "질병 ID") Long diseaseId,
        @Schema(description = "질병명") String diseaseName,
        @Schema(description = "대상 종", example = "DOG") Species species,
        @Schema(description = "카테고리") String category,
        @Schema(description = "대표 이미지 URL") String diseaseImageUrl,
        @Schema(description = "기본 설명") String description,
        @Schema(description = "증상") String symptoms,
        @Schema(description = "원인") String cause,
        @Schema(description = "홈 케어") String careGuide,
        @Schema(description = "예방법") String prevention,
        @Schema(description = "치료 및 관리") String treatment,
        @Schema(description = "관련 질환") String relatedDiseases,
        @Schema(description = "경미 단계 예시 사진") String mildExampleImageUrl,
        @Schema(description = "주의 단계 예시 사진") String cautionExampleImageUrl,
        @Schema(description = "위험 단계 예시 사진") String dangerExampleImageUrl
) {
    public static DiseaseResponse from(Disease d) {
        return new DiseaseResponse(
                d.getDiseaseId(),
                d.getDiseaseName(),
                d.getSpecies(),
                d.getCategory(),
                d.getDiseaseImageUrl(),
                d.getDescription(),
                d.getSymptoms(),
                d.getCause(),
                d.getCareGuide(),
                d.getPrevention(),
                d.getTreatment(),
                d.getRelatedDiseases(),
                d.getMildExampleImageUrl(),
                d.getCautionExampleImageUrl(),
                d.getDangerExampleImageUrl()
        );
    }
}
