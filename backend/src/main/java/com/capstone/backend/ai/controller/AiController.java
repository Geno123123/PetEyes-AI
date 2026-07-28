package com.capstone.backend.ai.controller;

import com.capstone.backend.ai.dto.AiDiagnosisResult;
import com.capstone.backend.ai.dto.AiKeywordsResponse;
import com.capstone.backend.ai.dto.AiReportResponse;
import com.capstone.backend.ai.service.AiService;
import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.entity.type.EyePosition;
import com.capstone.backend.image.dto.ImageUploadResponse;
import com.capstone.backend.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI 서버 연동 API")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiService aiService;
    private final ImageService imageService;

    @Operation(
            summary = "안구 사진 진단 + 문진서 생성",
            description = "이미지를 업로드하면 AI가 질병을 진단하고 문진서를 생성합니다. S3 업로드 → AI 진단 → DB 저장 → 문진서 생성을 한 번에 처리합니다."
    )
    @PostMapping(value = "/pets/{petId}/diagnose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AiDiagnosisResult diagnose(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId,
            @RequestPart("image") MultipartFile image,
            @RequestParam("eye_position") EyePosition eyePosition,
            @RequestParam(value = "pet_type", defaultValue = "dog") String petType,
            @RequestParam(value = "crop_mode", defaultValue = "YOLO") String cropMode,
            @RequestParam(value = "q1", defaultValue = "0") int q1,
            @RequestParam(value = "q2", defaultValue = "0") int q2,
            @RequestParam(value = "q3", defaultValue = "0") int q3,
            @RequestParam(value = "q4", defaultValue = "0") int q4,
            @RequestParam(value = "q5", defaultValue = "0") int q5,
            @RequestParam(value = "q6", defaultValue = "0") int q6,
            @RequestParam(value = "q7", defaultValue = "0") int q7,
            @RequestParam(value = "q8", defaultValue = "0") int q8,
            @RequestParam(value = "q9", defaultValue = "0") int q9,
            @RequestParam(value = "q10", defaultValue = "0") int q10
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        ImageUploadResponse uploaded = imageService.upload(principal, image);
        return aiService.diagnoseAndSave(
                principal.id(), petId, image, uploaded.imageUrl(), eyePosition, petType, cropMode,
                q1, q2, q3, q4, q5, q6, q7, q8, q9, q10
        );
    }

    @Operation(
            summary = "진단 기반 문진서 재생성",
            description = "기존 진단 결과로 AI 문진서를 다시 생성합니다."
    )
    @GetMapping("/pets/{petId}/diagnoses/{diagnosisId}/report")
    public AiReportResponse getReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId,
            @PathVariable Long diagnosisId
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return aiService.getReport(principal.id(), petId, diagnosisId);
    }

    @Operation(
            summary = "병원 리뷰 키워드 추출",
            description = "병원에 등록된 리뷰를 AI가 분석해 핵심 키워드를 반환합니다."
    )
    @GetMapping("/hospitals/{hospitalId}/keywords")
    public AiKeywordsResponse extractKeywords(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long hospitalId
    ) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return aiService.extractKeywords(hospitalId);
    }
}
