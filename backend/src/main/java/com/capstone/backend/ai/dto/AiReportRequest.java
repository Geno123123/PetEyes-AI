package com.capstone.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiReportRequest(
        @JsonProperty("pet_type") String petType,
        String disease,
        @JsonProperty("severity_label") String severityLabel,
        double score
) {}
