package com.capstone.backend.diagnosis.dto;

public record DiagnosisDiseaseInfoResponse(
        Long diagnosisId,
        Long diseaseId,
        String diseaseName,
        String diseaseImageUrl
) {
}
