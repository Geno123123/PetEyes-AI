package com.capstone.backend.qna.dto;

import com.capstone.backend.entity.QnaAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 답변 목록 응답")
public record QnaMyAnswerResponse(
        @Schema(description = "답변 ID") Long answerId,
        @Schema(description = "게시글 ID") Long postId,
        @Schema(description = "게시글 제목") String postTitle,
        @Schema(description = "답변 내용") String content,
        @Schema(description = "수의사 답변 여부") boolean vet,
        @Schema(description = "답변 작성일시") LocalDateTime createdAt
) {
    public static QnaMyAnswerResponse from(QnaAnswer answer) {
        return new QnaMyAnswerResponse(
                answer.getId(),
                answer.getPost().getId(),
                answer.getPost().getTitle(),
                answer.getContent(),
                answer.isVet(),
                answer.getCreatedAt()
        );
    }
}
