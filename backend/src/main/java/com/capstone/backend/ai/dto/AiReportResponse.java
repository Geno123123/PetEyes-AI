package com.capstone.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiReportResponse(
        @JsonProperty("pet_type") String petType,
        String disease,
        @JsonProperty("severity_label") String severityLabel,
        double score,
        @JsonProperty("rag_chunks") int ragChunks,
        String report
) {}
