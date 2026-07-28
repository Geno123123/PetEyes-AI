package com.capstone.backend.disease.dto;

import com.capstone.backend.entity.Disease;
import com.capstone.backend.entity.type.Species;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "질병 목록 요약 응답")
public record DiseaseSummaryResponse(
        @Schema(description = "질병 ID") Long diseaseId,
        @Schema(description = "질병명") String diseaseName,
        @Schema(description = "대상 종", example = "DOG") Species species,
        @Schema(description = "카테고리") String category,
        @Schema(description = "대표 이미지 URL") String diseaseImageUrl,
        @Schema(description = "증상 요약") String symptoms
) {
    public static DiseaseSummaryResponse from(Disease d) {
        return new DiseaseSummaryResponse(
                d.getDiseaseId(),
                d.getDiseaseName(),
                d.getSpecies(),
                d.getCategory(),
                d.getDiseaseImageUrl(),
                d.getSymptoms()
        );
    }
}
