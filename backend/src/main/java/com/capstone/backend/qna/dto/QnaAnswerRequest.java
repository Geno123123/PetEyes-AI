package com.capstone.backend.qna.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Q&A 답변 작성 요청")
public record QnaAnswerRequest(
        @Schema(description = "답변 내용") String content
) {}
