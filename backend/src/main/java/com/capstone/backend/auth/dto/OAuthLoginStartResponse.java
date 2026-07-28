package com.capstone.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OAuthLoginStartResponse(
        @Schema(description = "OAuth 로그인 시작 URL", example = "https://api.campustable.shop/api/oauth2/authorization/naver")
        String authorizationUrl
) {
}
