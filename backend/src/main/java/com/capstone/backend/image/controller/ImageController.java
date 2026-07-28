package com.capstone.backend.image.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.image.dto.ImageUploadResponse;
import com.capstone.backend.image.service.ImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "이미지 업로드 API")
@SecurityRequirement(name = "bearerAuth")
public class ImageController implements ImageControllerDocs {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imageService.upload(principal, file));
    }
}
