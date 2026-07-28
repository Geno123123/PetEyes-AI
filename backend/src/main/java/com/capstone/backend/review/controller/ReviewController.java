package com.capstone.backend.review.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.repository.HospitalRepository;
import com.capstone.backend.review.dto.ReviewCreateRequest;
import com.capstone.backend.review.dto.ReviewResponse;
import com.capstone.backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "병원 리뷰 API")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;
    private final HospitalRepository hospitalRepository;

    @Operation(summary = "리뷰 작성", description = "병원 ID로 리뷰를 작성합니다. 한 유저는 같은 병원에 리뷰를 한 번만 작성할 수 있습니다.")
    @PostMapping("/{hospitalId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long hospitalId,
            @RequestBody @Valid ReviewCreateRequest request
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(principal.id(), hospitalId, request));
    }

    @Operation(summary = "병원 리뷰 목록 조회")
    @GetMapping("/{hospitalId}/reviews")
    public List<ReviewResponse> getReviews(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long hospitalId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return reviewService.getReviews(principal.id(), hospitalId);
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰만 수정할 수 있습니다.")
    @PutMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewCreateRequest request
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return reviewService.updateReview(principal.id(), reviewId, request);
    }

    @Operation(summary = "병원 키워드 조회", description = "AI가 추출해 저장된 병원 키워드를 반환합니다.")
    @GetMapping("/{hospitalId}/keywords")
    public List<String> getKeywords(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long hospitalId
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"))
                .getKeywordList();
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰만 삭제할 수 있습니다.")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reviewId
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        reviewService.deleteReview(principal.id(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
