package com.capstone.backend.auth.controller;

import com.capstone.backend.auth.dto.AuthCodeTokenRequest;
import com.capstone.backend.auth.dto.LoginRequest;
import com.capstone.backend.auth.dto.LoginTokenResponse;
import com.capstone.backend.auth.dto.OAuthLoginStartResponse;
import com.capstone.backend.auth.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface AuthControllerDocs {

    @Operation(
            summary = "일반 회원가입",
            description = "이메일/비밀번호 기반 일반 회원가입을 처리하고 access token을 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "회원가입 요청 정보",
            content = @Content(
                    schema = @Schema(implementation = SignupRequest.class),
                    examples = @ExampleObject(
                            name = "signup",
                            value = """
                                    {
                                      \"email\": \"user@example.com\",
                                      \"password\": \"password1234\",
                                      \"name\": \"홍길동\",
                                      \"nickname\": \"길동이\",
                                      \"phoneNumber\": \"010-1234-5678\"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = LoginTokenResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    ResponseEntity<LoginTokenResponse> signup(SignupRequest request);

    @Operation(
            summary = "일반 로그인",
            description = "이메일/비밀번호 기반 일반 로그인 후 access token을 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "로그인 요청 정보",
            content = @Content(
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "login",
                            value = """
                                    {
                                      \"email\": \"user@example.com\",
                                      \"password\": \"password1234\"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginTokenResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않음")
    })
    ResponseEntity<LoginTokenResponse> login(LoginRequest request);

    @Operation(
            summary = "네이버 로그인 시작 URL 조회",
            description = "프론트엔드가 이 URL로 이동하면 네이버 OAuth 로그인 화면으로 리다이렉트됩니다. redirectUri 쿼리를 전달하면 로그인 성공 후 해당 URI로 최종 리다이렉트됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "네이버 로그인 URL 반환",
                    content = @Content(schema = @Schema(implementation = OAuthLoginStartResponse.class))
            )
    })
    ResponseEntity<OAuthLoginStartResponse> naverLoginUrl(
            @RequestParam(value = "redirectUri", required = false) String redirectUri
    );

    @Operation(
            summary = "카카오 로그인 시작 URL 조회",
            description = "프론트엔드가 이 URL로 이동하면 카카오 OAuth 로그인 화면으로 리다이렉트됩니다. redirectUri 쿼리를 전달하면 로그인 성공 후 해당 URI로 최종 리다이렉트됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "카카오 로그인 URL 반환",
                    content = @Content(schema = @Schema(implementation = OAuthLoginStartResponse.class))
            )
    })
    ResponseEntity<OAuthLoginStartResponse> kakaoLoginUrl(
            @RequestParam(value = "redirectUri", required = false) String redirectUri
    );

    @Operation(
            summary = "인가 코드로 access token 발급",
            description = "소셜 로그인 처리 통합 API. code를 보내면 기존회원 JWT를 발급하고, signupCode를 보내면 신규회원 가입 완료 후 JWT를 발급합니다."
    )
    @RequestBody(
            required = true,
            description = "일회성 인증 코드",
            content = @Content(
                    schema = @Schema(implementation = AuthCodeTokenRequest.class),
                    examples = @ExampleObject(
                            name = "exchange",
                            value = """
                                    {
                                      \"code\": \"a1b2c3d4e5\",
                                      \"signupCode\": null,
                                      \"name\": null,
                                      \"nickname\": null,
                                      \"phoneNumber\": null
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 발급 성공",
                    content = @Content(schema = @Schema(implementation = LoginTokenResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "code/signupCode 누락 등 잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 code/signupCode")
    })
    ResponseEntity<LoginTokenResponse> exchange(AuthCodeTokenRequest request);
}
