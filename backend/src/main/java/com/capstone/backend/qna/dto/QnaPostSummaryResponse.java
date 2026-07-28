package com.capstone.backend.qna.dto;

import com.capstone.backend.entity.QnaPost;
import com.capstone.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Q&A 게시글 목록 응답")
public record QnaPostSummaryResponse(
        @Schema(description = "게시글 ID") Long id,
        @Schema(description = "제목") String title,
        @Schema(description = "내용 미리보기 (50자)") String preview,
        @Schema(description = "반려동물 종류") String species,
        @Schema(description = "답변 완료 여부") boolean answered,
        @Schema(description = "답변 수") int answerCount,
        @Schema(description = "작성일시") LocalDateTime createdAt,
        @Schema(description = "내 게시글 여부") boolean mine,
        @Schema(description = "작성자 닉네임") String authorNickname,
        @Schema(description = "작성자 프로필 이미지 URL") String authorProfileImageUrl
) {
    public static QnaPostSummaryResponse from(QnaPost p, Long currentUserId, User author) {
        String content = p.getContent();
        String preview = content.length() > 50 ? content.substring(0, 50) + "···" : content;
        return new QnaPostSummaryResponse(
                p.getId(),
                p.getTitle(),
                preview,
                p.getSpecies() != null ? p.getSpecies().name() : null,
                p.isAnswered(),
                p.getAnswers().size(),
                p.getCreatedAt(),
                p.getUserId().equals(currentUserId),
                author != null ? author.getNickname() : null,
                author != null ? author.getProfileImageUrl() : null
        );
    }
}
