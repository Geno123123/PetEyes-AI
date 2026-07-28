package com.capstone.backend.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "ImageUploadRequest", description = "이미지 업로드 요청")
public record ImageUploadRequest(
        @Schema(
                description = "업로드할 이미지 파일",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        MultipartFile file
) {
}
