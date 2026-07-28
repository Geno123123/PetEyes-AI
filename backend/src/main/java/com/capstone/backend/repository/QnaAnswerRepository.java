package com.capstone.backend.repository;

import com.capstone.backend.entity.QnaAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {

    List<QnaAnswer> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    List<QnaAnswer> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
