package com.capstone.backend.image.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.image.dto.ImageUploadRequest;
import com.capstone.backend.image.dto.ImageUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ImageControllerDocs {

    @Operation(
            summary = "이미지 업로드",
            description = "반려동물 사진 이미지를 S3에 업로드하고 접근 가능한 URL을 반환합니다."
    )
    @RequestBody(
            required = true,
            description = "업로드할 이미지 파일",
            content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(implementation = ImageUploadRequest.class)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "업로드 성공",
                    content = @Content(
                            schema = @Schema(implementation = ImageUploadResponse.class),
                            examples = @ExampleObject(
                                    name = "image-upload-success",
                                    value = """
                                            {
                                              "key": "uploads/users/1/20260327/550e8400-e29b-41d4-a716-446655440000.jpg",
                                              "imageUrl": "https://my-bucket.s3.ap-northeast-2.amazonaws.com/uploads/users/1/20260327/550e8400-e29b-41d4-a716-446655440000.jpg",
                                              "originalName": "pet-eye.jpg",
                                              "contentType": "image/jpeg",
                                              "size": 245331
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "413", description = "허용 크기를 초과한 파일"),
            @ApiResponse(responseCode = "500", description = "S3 업로드 실패")
    })
    ResponseEntity<ImageUploadResponse> uploadImage(
            @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(hidden = true) MultipartFile file
    );
}
