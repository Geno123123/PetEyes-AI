package com.capstone.backend.admin.dto;

import com.capstone.backend.entity.Review;

public record AdminReviewResponse(
        Long reviewId,
        Long hospitalId,
        String hospitalName,
        Long userId,
        String review,
        Double rating
) {
    public static AdminReviewResponse from(Review r, String hospitalName) {
        return new AdminReviewResponse(
                r.getReviewId(),
                r.getHospitalId(),
                hospitalName,
                r.getUserId(),
                r.getReview(),
                r.getRating()
        );
    }
}
