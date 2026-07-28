package com.capstone.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String review;

    @Column(nullable = false)
    private Double rating;

    @Column(name = "cost_rating", nullable = false)
    private Double costRating;

    @Column(name = "expertise_rating", nullable = false)
    private Double expertiseRating;

    @Column(name = "service_rating", nullable = false)
    private Double serviceRating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void update(String review, Double costRating, Double expertiseRating, Double serviceRating) {
        this.review = review;
        this.costRating = costRating;
        this.expertiseRating = expertiseRating;
        this.serviceRating = serviceRating;
        this.rating = (costRating + expertiseRating + serviceRating) / 3.0;
    }
}
