package com.capstone.backend.ai.client;

import com.capstone.backend.ai.dto.AiDiagnoseResponse;
import com.capstone.backend.ai.dto.AiKeywordsRequest;
import com.capstone.backend.ai.dto.AiKeywordsResponse;
import com.capstone.backend.ai.dto.AiReportRequest;
import com.capstone.backend.ai.dto.AiReportResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
public class AiServerClient {

    private final RestClient restClient;

    public AiServerClient(@Qualifier("aiServerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public AiDiagnoseResponse diagnose(
            MultipartFile image, String petType, String cropMode,
            int q1, int q2, int q3, int q4, int q5,
            int q6, int q7, int q8, int q9, int q10
    ) {
        ByteArrayResource fileResource;
        try {
            byte[] bytes = image.getBytes();
            String filename = image.getOriginalFilename();
            fileResource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return (filename != null && !filename.isBlank()) ? filename : "eye.jpg";
                }
            };
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read image file", e);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", fileResource);
        String normalizedPetType = petType.toLowerCase(Locale.ROOT);
        body.add("pet_type", normalizedPetType);
        body.add("crop_mode", cropMode);
        body.add("q1", q1);
        body.add("q2", q2);
        body.add("q3", q3);
        body.add("q4", q4);
        body.add("q5", q5);
        body.add("q6", q6);
        body.add("q7", q7);
        body.add("q8", q8);
        body.add("q9", q9);
        body.add("q10", q10);

        try {
            log.info(
                    "[AI] /diagnose 요청 시작 - petType={}, cropMode={}, q=[{},{},{},{},{},{},{},{},{},{}]",
                    normalizedPetType, cropMode, q1, q2, q3, q4, q5, q6, q7, q8, q9, q10
            );
            AiDiagnoseResponse response = restClient.post()
                    .uri("/diagnose")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(AiDiagnoseResponse.class);
            log.info("[AI] /diagnose 응답 성공 - detected={}", response != null ? response.detected() : "null");
            return response;
        } catch (HttpClientErrorException e) {
            log.error("[AI] /diagnose 요청 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new ResponseStatusException(e.getStatusCode(), extractDetail(e.getResponseBodyAsString()), e);
        } catch (RestClientException e) {
            log.error("[AI] /diagnose 요청 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버 연결 실패", e);
        }
    }

    public AiReportResponse generateReport(String petType, String disease, String severityLabel, double score) {
        AiReportRequest request = new AiReportRequest(petType, disease, severityLabel, score);
        try {
            return restClient.post()
                    .uri("/report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiReportResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), extractDetail(e.getResponseBodyAsString()), e);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버 연결 실패", e);
        }
    }

    public AiKeywordsResponse extractKeywords(Long hospitalId, List<String> reviews) {
        AiKeywordsRequest request = new AiKeywordsRequest(hospitalId, reviews);
        try {
            return restClient.post()
                    .uri("/extract-keywords")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiKeywordsResponse.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), extractDetail(e.getResponseBodyAsString()), e);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 서버 연결 실패", e);
        }
    }

    private String extractDetail(String responseBody) {
        if (responseBody == null) return "AI 서버 오류";
        int start = responseBody.indexOf("\"detail\":\"");
        if (start == -1) return responseBody;
        start += 10;
        int end = responseBody.indexOf("\"", start);
        return end == -1 ? responseBody : responseBody.substring(start, end);
    }
}
