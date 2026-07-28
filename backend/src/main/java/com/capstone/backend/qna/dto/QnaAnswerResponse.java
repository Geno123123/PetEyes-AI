package com.capstone.backend.qna.dto;

import com.capstone.backend.entity.QnaAnswer;
import com.capstone.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Q&A 답변 응답")
public record QnaAnswerResponse(
        @Schema(description = "답변 ID") Long id,
        @Schema(description = "작성자 이름") String authorName,
        @Schema(description = "작성자 닉네임") String authorNickname,
        @Schema(description = "작성자 프로필 이미지 URL") String authorProfileImageUrl,
        @Schema(description = "수의사 여부") boolean vet,
        @Schema(description = "답변 내용") String content,
        @Schema(description = "작성일시") LocalDateTime createdAt,
        @Schema(description = "내 답변 여부") boolean mine
) {
    public static QnaAnswerResponse from(QnaAnswer a, Long currentUserId, User author) {
        return new QnaAnswerResponse(
                a.getId(),
                a.getAuthorName(),
                author != null ? author.getNickname() : null,
                author != null ? author.getProfileImageUrl() : null,
                a.isVet(),
                a.getContent(),
                a.getCreatedAt(),
                a.getUserId().equals(currentUserId)
        );
    }
}
