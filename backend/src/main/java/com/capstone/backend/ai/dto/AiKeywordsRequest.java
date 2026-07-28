package com.capstone.backend.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiKeywordsRequest(
        @JsonProperty("hospital_id") Long hospitalId,
        List<String> reviews
) {}
