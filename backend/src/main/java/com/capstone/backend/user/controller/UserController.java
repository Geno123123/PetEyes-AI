package com.capstone.backend.user.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.image.dto.ImageUploadResponse;
import com.capstone.backend.image.service.ImageService;
import com.capstone.backend.user.dto.ChangePasswordRequest;
import com.capstone.backend.user.dto.UpdateUserRequest;
import com.capstone.backend.user.dto.UserMeResponse;
import com.capstone.backend.user.dto.VetVerificationRequest;
import com.capstone.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "사용자 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 access token 기준 사용자 정보를 조회합니다.")
    public UserMeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getMe(principal);
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "이름/전화번호를 수정합니다.")
    public UserMeResponse updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateUserRequest request
    ) {
        return userService.updateMe(principal, request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "비밀번호 변경")
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(principal, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "회원 탈퇴")
    public void deleteMe(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteMe(principal);
    }

    @PostMapping("/me/vet-verification")
    @Operation(summary = "수의사 인증 신청", description = "수의사 인증 신청을 생성/갱신하고 상태를 PENDING으로 변경합니다.")
    public UserMeResponse requestVetVerification(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody VetVerificationRequest request
    ) {
        return userService.requestVetVerification(principal, request);
    }

    @PostMapping(value = "/me/vet-verification/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "수의사 인증 신청(단일 호출)", description = "자격번호와 인증 이미지를 한 번에 업로드하고 수의사 인증을 신청합니다.")
    public UserMeResponse requestVetVerificationWithImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("licenseNumber") String licenseNumber,
            @RequestPart("file") MultipartFile file
    ) {
        ImageUploadResponse uploaded = imageService.uploadVetProof(principal, file);
        VetVerificationRequest request = new VetVerificationRequest(licenseNumber, uploaded.imageUrl());
        return userService.requestVetVerification(principal, request);
    }

    @PostMapping(value = "/me/vet-verification/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "수의사 인증 이미지 업로드", description = "수의사 인증 증빙 이미지를 업로드하고 URL을 반환합니다.")
    public ImageUploadResponse uploadVetVerificationImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        return imageService.uploadVetProof(principal, file);
    }
}
