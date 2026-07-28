package com.capstone.backend.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드 결과")
public record ImageUploadResponse(
        @Schema(description = "S3 객체 키", example = "uploads/users/1/20260327/550e8400-e29b-41d4-a716-446655440000.jpg")
        String key,
        @Schema(description = "업로드된 이미지 접근 URL", example = "https://my-bucket.s3.ap-northeast-2.amazonaws.com/uploads/users/1/20260327/550e8400-e29b-41d4-a716-446655440000.jpg")
        String imageUrl,
        @Schema(description = "원본 파일명", example = "pet-eye.jpg")
        String originalName,
        @Schema(description = "파일 MIME 타입", example = "image/jpeg")
        String contentType,
        @Schema(description = "파일 크기(byte)", example = "245331")
        long size
) {
}
