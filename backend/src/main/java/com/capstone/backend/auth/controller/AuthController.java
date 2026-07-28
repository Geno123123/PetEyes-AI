package com.capstone.backend.auth.controller;

import com.capstone.backend.auth.dto.AuthCodeTokenRequest;
import com.capstone.backend.auth.dto.LoginRequest;
import com.capstone.backend.auth.dto.LoginTokenResponse;
import com.capstone.backend.auth.dto.OAuthLoginStartResponse;
import com.capstone.backend.auth.dto.SignupRequest;
import com.capstone.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    private static final String NAVER_OAUTH_BASE_URL = "https://api.campustable.shop";

    @PostMapping("/signup")
    @Override
    public ResponseEntity<LoginTokenResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<LoginTokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/naver/url")
    @Override
    public ResponseEntity<OAuthLoginStartResponse> naverLoginUrl(@RequestParam(value = "redirectUri", required = false) String redirectUri) {
        String authorizationUrl = buildAuthorizationUrl("naver", redirectUri);
        return ResponseEntity.ok(new OAuthLoginStartResponse(authorizationUrl));
    }

    @GetMapping("/kakao/url")
    @Override
    public ResponseEntity<OAuthLoginStartResponse> kakaoLoginUrl(@RequestParam(value = "redirectUri", required = false) String redirectUri) {
        String authorizationUrl = buildAuthorizationUrl("kakao", redirectUri);
        return ResponseEntity.ok(new OAuthLoginStartResponse(authorizationUrl));
    }

    @PostMapping("/token")
    @Override
    public ResponseEntity<LoginTokenResponse> exchange(@RequestBody AuthCodeTokenRequest request) {
        return ResponseEntity.ok(authService.exchangeSocialToken(request));
    }

    private String buildAuthorizationUrl(String provider, String redirectUri) {
        String base = NAVER_OAUTH_BASE_URL + "/api/oauth2/authorization/" + provider;
        if (redirectUri == null || redirectUri.isBlank()) {
            return base;
        }
        return base + "?redirectUri=" + URLEncoder.encode(redirectUri.trim(), StandardCharsets.UTF_8);
    }
}
