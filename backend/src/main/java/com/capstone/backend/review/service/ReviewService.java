package com.capstone.backend.review.service;

import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.User;
import com.capstone.backend.repository.HospitalRepository;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
import com.capstone.backend.review.dto.ReviewCreateRequest;
import com.capstone.backend.review.dto.ReviewResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(Long userId, Long hospitalId, ReviewCreateRequest request) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found");
        }

        Review saved = reviewRepository.save(Review.builder()
                .hospitalId(hospitalId)
                .userId(userId)
                .review(request.review())
                .costRating(request.costRating())
                .expertiseRating(request.expertiseRating())
                .serviceRating(request.serviceRating())
                .rating(averageRating(request.costRating(), request.expertiseRating(), request.serviceRating()))
                .build());

        User author = userRepository.findById(userId).orElse(null);
        return ReviewResponse.from(saved, author, userId);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long userId, Long hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found");
        }
        List<Review> reviews = reviewRepository.findByHospitalId(hospitalId);
        Set<Long> userIds = reviews.stream().map(Review::getUserId).collect(Collectors.toSet());
        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return reviews.stream()
                .map(review -> ReviewResponse.from(review, usersById.get(review.getUserId()), userId))
                .toList();
    }

    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewCreateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's review");
        }

        review.update(
                request.review(),
                request.costRating(),
                request.expertiseRating(),
                request.serviceRating()
        );
        User author = userRepository.findById(userId).orElse(null);
        return ReviewResponse.from(review, author, userId);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user's review");
        }

        reviewRepository.delete(review);
    }

    private double averageRating(double costRating, double expertiseRating, double serviceRating) {
        return (costRating + expertiseRating + serviceRating) / 3.0;
    }
}
