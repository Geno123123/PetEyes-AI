package com.capstone.backend.disease.service;

import com.capstone.backend.disease.dto.DiseaseResponse;
import com.capstone.backend.disease.dto.DiseaseSummaryResponse;
import com.capstone.backend.repository.DiseaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    @Transactional(readOnly = true)
    public List<DiseaseSummaryResponse> getAllDiseases() {
        return diseaseRepository.findAll().stream()
                .map(DiseaseSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DiseaseResponse getDisease(Long diseaseId) {
        return diseaseRepository.findById(diseaseId)
                .map(DiseaseResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disease not found"));
    }
}
