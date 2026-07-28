package com.capstone.backend.repository;

import com.capstone.backend.entity.QnaPost;
import com.capstone.backend.entity.type.Species;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaPostRepository extends JpaRepository<QnaPost, Long> {

    @Query("""
            SELECT p FROM QnaPost p
            WHERE (:species IS NULL OR p.species = :species)
              AND (:answered IS NULL OR p.answered = :answered)
            ORDER BY p.createdAt DESC
            """)
    List<QnaPost> findAllByFilter(
            @Param("species") Species species,
            @Param("answered") Boolean answered
    );

    List<QnaPost> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
