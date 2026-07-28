package com.capstone.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiKeywordsResponse(
        @JsonProperty("hospital_id") Long hospitalId,
        List<String> keywords
) {}
