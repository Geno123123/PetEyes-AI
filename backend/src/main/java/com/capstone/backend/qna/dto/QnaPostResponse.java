package com.capstone.backend.qna.dto;

import com.capstone.backend.entity.QnaPost;
import com.capstone.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Q&A 게시글 상세 응답")
public record QnaPostResponse(
        @Schema(description = "게시글 ID") Long id,
        @Schema(description = "제목") String title,
        @Schema(description = "내용") String content,
        @Schema(description = "반려동물 종류") String species,
        @Schema(description = "답변 완료 여부") boolean answered,
        @Schema(description = "작성일시") LocalDateTime createdAt,
        @Schema(description = "내 게시글 여부") boolean mine,
        @Schema(description = "작성자 닉네임") String authorNickname,
        @Schema(description = "작성자 프로필 이미지 URL") String authorProfileImageUrl,
        @Schema(description = "답변 목록") List<QnaAnswerResponse> answers
) {
    public static QnaPostResponse from(QnaPost p, Long currentUserId, User author, Map<Long, User> answerAuthorMap) {
        List<QnaAnswerResponse> answers = p.getAnswers().stream()
                .map(a -> QnaAnswerResponse.from(a, currentUserId, answerAuthorMap.get(a.getUserId())))
                .toList();
        return new QnaPostResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getSpecies() != null ? p.getSpecies().name() : null,
                p.isAnswered(),
                p.getCreatedAt(),
                p.getUserId().equals(currentUserId),
                author != null ? author.getNickname() : null,
                author != null ? author.getProfileImageUrl() : null,
                answers
        );
    }
}
