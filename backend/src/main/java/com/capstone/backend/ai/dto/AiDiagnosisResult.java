package com.capstone.backend.ai.dto;

import com.capstone.backend.diagnosis.dto.DiagnosisResponse;

public record AiDiagnosisResult(
        DiagnosisResponse diagnosis,
        String report,
        String croppedImage
) {}
