package com.capstone.backend.image.service;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.config.S3Properties;
import com.capstone.backend.image.dto.ImageUploadResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Transactional(readOnly = true)
    public ImageUploadResponse upload(UserPrincipal principal, MultipartFile file) {
        requireUser(principal);
        validateS3Configuration();
        validateFile(file);

        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String contentType = file.getContentType();
        String key = buildObjectKey("users", principal.id(), originalName);

        return uploadInternal(file, originalName, contentType, key);
    }

    @Transactional(readOnly = true)
    public ImageUploadResponse uploadVetProof(UserPrincipal principal, MultipartFile file) {
        requireUser(principal);
        validateS3Configuration();
        validateFile(file);

        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String contentType = file.getContentType();
        String key = buildObjectKey("vet-verification", principal.id(), originalName);

        return uploadInternal(file, originalName, contentType, key);
    }

    private ImageUploadResponse uploadInternal(MultipartFile file, String originalName, String contentType, String key) {

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read upload file", ex);
        } catch (S3Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image to S3", ex);
        }

        return new ImageUploadResponse(
                key,
                buildPublicUrl(key),
                originalName,
                contentType,
                file.getSize()
        );
    }

    private void requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only jpeg, png, webp images are allowed");
        }

        if (file.getSize() > s3Properties.maxFileSizeBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image file is too large");
        }
    }

    private void validateS3Configuration() {
        if (isBlank(s3Properties.bucket())
                || isBlank(s3Properties.region())
                || isBlank(s3Properties.accessKey())
                || isBlank(s3Properties.secretKey())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 configuration is incomplete");
        }
    }

    private String sanitizeOriginalName(String originalName) {
        String fallback = "image";
        if (originalName == null || originalName.isBlank()) {
            return fallback;
        }

        String sanitized = originalName.replace("\\", "/");
        int lastSlashIndex = sanitized.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            sanitized = sanitized.substring(lastSlashIndex + 1);
        }

        sanitized = sanitized.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    private String buildObjectKey(String topDir, Long userId, String originalName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String extension = extractExtension(originalName);
        String prefix = normalizePrefix(s3Properties.keyPrefix());
        return prefix
                + topDir + "/" + userId + "/"
                + datePath + "/"
                + UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String normalizePrefix(String keyPrefix) {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return "";
        }
        String trimmed = keyPrefix.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private String buildPublicUrl(String key) {
        if (s3Properties.publicBaseUrl() != null && !s3Properties.publicBaseUrl().isBlank()) {
            String baseUrl = s3Properties.publicBaseUrl().endsWith("/")
                    ? s3Properties.publicBaseUrl().substring(0, s3Properties.publicBaseUrl().length() - 1)
                    : s3Properties.publicBaseUrl();
            return baseUrl + "/" + key;
        }

        return "https://" + s3Properties.bucket() + ".s3." + s3Properties.region() + ".amazonaws.com/" + key;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
