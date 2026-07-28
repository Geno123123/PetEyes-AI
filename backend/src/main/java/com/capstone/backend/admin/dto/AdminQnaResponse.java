package com.capstone.backend.admin.dto;

import com.capstone.backend.entity.QnaPost;
import java.time.LocalDateTime;

public record AdminQnaResponse(
        Long id,
        Long userId,
        String title,
        String species,
        boolean answered,
        int answerCount,
        LocalDateTime createdAt
) {
    public static AdminQnaResponse from(QnaPost p) {
        return new AdminQnaResponse(
                p.getId(),
                p.getUserId(),
                p.getTitle(),
                p.getSpecies() != null ? p.getSpecies().name() : null,
                p.isAnswered(),
                p.getAnswers().size(),
                p.getCreatedAt()
        );
    }
}
