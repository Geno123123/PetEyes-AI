package com.capstone.backend.qna.dto;

import com.capstone.backend.entity.type.Species;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Q&A 질문 작성 요청")
public record QnaPostRequest(
        @Schema(description = "제목") String title,
        @Schema(description = "내용") String content,
        @Schema(description = "반려동물 종류 (DOG / CAT)", nullable = true) Species species
) {}
