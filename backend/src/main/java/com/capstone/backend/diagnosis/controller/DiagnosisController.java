package com.capstone.backend.diagnosis.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.diagnosis.dto.DiagnosisRequest;
import com.capstone.backend.diagnosis.dto.DiagnosisResponse;
import com.capstone.backend.diagnosis.dto.DiagnosisTrendResponse;
import com.capstone.backend.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pets/{petId}/diagnoses")
@RequiredArgsConstructor
@Tag(name = "Diagnoses", description = "진단 API")
@SecurityRequirement(name = "bearerAuth")
public class DiagnosisController implements DiagnosisControllerDocs {

    private final DiagnosisService diagnosisService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public DiagnosisResponse createDiagnosis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId,
            @RequestBody DiagnosisRequest request
    ) {
        return diagnosisService.createDiagnosis(requireUserId(principal), petId, request);
    }

    @GetMapping
    @Override
    public List<DiagnosisResponse> getDiagnoses(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId
    ) {
        return diagnosisService.getDiagnoses(requireUserId(principal), petId);
    }

    @GetMapping("/{diagnosisId}")
    @Override
    public DiagnosisResponse getDiagnosis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId,
            @PathVariable Long diagnosisId
    ) {
        return diagnosisService.getDiagnosis(requireUserId(principal), petId, diagnosisId);
    }

    @DeleteMapping("/{diagnosisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void deleteDiagnosis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId,
            @PathVariable Long diagnosisId
    ) {
        diagnosisService.deleteDiagnosis(requireUserId(principal), petId, diagnosisId);
    }

    @GetMapping("/trend")
    @Override
    public DiagnosisTrendResponse getTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId
    ) {
        return diagnosisService.getTrend(requireUserId(principal), petId);
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return principal.id();
    }
}
