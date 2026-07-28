package com.capstone.backend.review.dto;

import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "리뷰 조회 응답")
public record ReviewResponse(

        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,

        @Schema(description = "병원 ID", example = "42")
        Long hospitalId,

        @Schema(description = "작성자 ID", example = "7")
        Long userId,

        @Schema(description = "작성자 닉네임", example = "길동이")
        String authorNickname,

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/profile/7.jpg")
        String authorProfileImageUrl,

        @Schema(description = "리뷰 내용", example = "친절하고 시설이 깨끗해요.")
        String review,

        @Schema(description = "평균 평점", example = "4.5")
        Double averageRating,

        @Schema(description = "비용 합리성 평점", example = "4.0")
        Double costRating,

        @Schema(description = "진료 전문성 평점", example = "4.5")
        Double expertiseRating,

        @Schema(description = "서비스/환경 평점", example = "5.0")
        Double serviceRating,

        @Schema(description = "평균 평점(호환용)", example = "4.5")
        Double rating,

        @Schema(description = "작성 시각", example = "2026-05-14T21:30:00")
        LocalDateTime createdAt,

        @Schema(description = "현재 로그인 사용자의 리뷰 여부", example = "true")
        boolean mine
) {

    public static ReviewResponse from(Review review, User author, Long currentUserId) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getHospitalId(),
                review.getUserId(),
                author != null ? author.getNickname() : null,
                author != null ? author.getProfileImageUrl() : null,
                review.getReview(),
                review.getRating(),
                review.getCostRating(),
                review.getExpertiseRating(),
                review.getServiceRating(),
                review.getRating(),
                review.getCreatedAt(),
                review.getUserId().equals(currentUserId)
        );
    }
}
