package com.capstone.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiDiagnoseResponse(
        boolean detected,
        double confidence,
        String disease,
        @JsonProperty("severity_label") String severityLabel,
        double score,
        @JsonProperty("rgb_hsv_score") Double rgbHsvScore,
        String report,
        @JsonProperty("cropped_image") String croppedImage
) {}
