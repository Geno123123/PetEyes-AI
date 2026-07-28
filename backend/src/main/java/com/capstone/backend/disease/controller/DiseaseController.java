package com.capstone.backend.disease.controller;

import com.capstone.backend.disease.dto.DiseaseResponse;
import com.capstone.backend.disease.dto.DiseaseSummaryResponse;
import com.capstone.backend.disease.service.DiseaseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
@Tag(name = "Diseases", description = "안구 질환 정보 API")
public class DiseaseController implements DiseaseControllerDocs {

    private final DiseaseService diseaseService;

    @GetMapping
    @Override
    public List<DiseaseSummaryResponse> getAllDiseases() {
        return diseaseService.getAllDiseases();
    }

    @GetMapping("/{diseaseId}")
    @Override
    public DiseaseResponse getDisease(@PathVariable Long diseaseId) {
        return diseaseService.getDisease(diseaseId);
    }
}
