package com.capstone.backend.repository;

import com.capstone.backend.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    void deleteByUserId(Long userId);

    List<Review> findByHospitalId(Long hospitalId);

    boolean existsByHospitalIdAndUserId(Long hospitalId, Long userId);

    @Query("""
            SELECT r.hospitalId AS hospitalId, AVG(r.rating) AS averageRating
            FROM Review r
            WHERE r.hospitalId IN :hospitalIds
            GROUP BY r.hospitalId
            """)
    List<HospitalAverageRatingProjection> findAverageRatingsByHospitalIds(@Param("hospitalIds") List<Long> hospitalIds);

    @Query("""
            SELECT r.hospitalId AS hospitalId, COUNT(r) AS reviewCount
            FROM Review r
            WHERE r.hospitalId IN :hospitalIds
            GROUP BY r.hospitalId
            """)
    List<HospitalReviewCountProjection> findReviewCountsByHospitalIds(@Param("hospitalIds") List<Long> hospitalIds);

    interface HospitalAverageRatingProjection {
        Long getHospitalId();
        Double getAverageRating();
    }

    interface HospitalReviewCountProjection {
        Long getHospitalId();
        Long getReviewCount();
    }
}
