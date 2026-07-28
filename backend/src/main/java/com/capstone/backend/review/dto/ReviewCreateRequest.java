package com.capstone.backend.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "리뷰 작성 요청")
public record ReviewCreateRequest(

        @Schema(description = "리뷰 내용", example = "친절하고 시설이 깨끗해요.")
        @NotBlank
        String review,

        @Schema(description = "비용 합리성 평점 (1.0 ~ 5.0)", example = "4.0")
        @NotNull
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        Double costRating,

        @Schema(description = "진료 전문성 평점 (1.0 ~ 5.0)", example = "4.5")
        @NotNull
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        Double expertiseRating,

        @Schema(description = "서비스/환경 평점 (1.0 ~ 5.0)", example = "5.0")
        @NotNull
        @DecimalMin("1.0")
        @DecimalMax("5.0")
        Double serviceRating
) {
}
