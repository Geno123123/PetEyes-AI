package com.capstone.backend.diagnosis.service;

import com.capstone.backend.diagnosis.dto.DiagnosisDiseaseInfoResponse;
import com.capstone.backend.diagnosis.dto.DiagnosisRequest;
import com.capstone.backend.diagnosis.dto.DiagnosisResponse;
import com.capstone.backend.diagnosis.dto.DiagnosisTrendResponse;
import com.capstone.backend.entity.Diagnosis;
import com.capstone.backend.entity.Disease;
import com.capstone.backend.entity.Pet;
import com.capstone.backend.entity.type.EyePosition;
import com.capstone.backend.repository.DiagnosisRepository;
import com.capstone.backend.repository.DiseaseRepository;
import com.capstone.backend.repository.PetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final DiseaseRepository diseaseRepository;
    private final PetRepository petRepository;

    @Transactional
    public DiagnosisResponse createDiagnosis(Long userId, Long petId, DiagnosisRequest request) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        Disease disease = diseaseRepository.findById(request.diseaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disease not found"));

        if (request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl is required");
        }
        if (request.eyePosition() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eyePosition is required");
        }
        if (request.rawSeverity() == null || request.rawSeverity().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rawSeverity is required");
        }
        if (request.confidence() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confidence is required");
        }

        Diagnosis diagnosis = Diagnosis.fromRaw(
                pet, disease,
                request.imageUrl(),
                request.eyePosition(),
                request.rawSeverity(),
                request.confidence()
        );

        return DiagnosisResponse.from(diagnosisRepository.save(diagnosis));
    }

    @Transactional(readOnly = true)
    public List<DiagnosisResponse> getDiagnoses(Long userId, Long petId) {
        validatePetOwnership(userId, petId);
        return diagnosisRepository.findAllByPetIdWithDisease(petId).stream()
                .map(DiagnosisResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DiagnosisResponse getDiagnosis(Long userId, Long petId, Long diagnosisId) {
        validatePetOwnership(userId, petId);
        Diagnosis diagnosis = diagnosisRepository.findByIdAndPetId(diagnosisId, petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnosis not found"));
        return DiagnosisResponse.from(diagnosis);
    }

    @Transactional
    public void deleteDiagnosis(Long userId, Long petId, Long diagnosisId) {
        validatePetOwnership(userId, petId);
        Diagnosis diagnosis = diagnosisRepository.findByIdAndPetId(diagnosisId, petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnosis not found"));
        diagnosisRepository.delete(diagnosis);
    }

    @Transactional(readOnly = true)
    public DiagnosisTrendResponse getTrend(Long userId, Long petId) {
        validatePetOwnership(userId, petId);
        List<Diagnosis> diagnoses = diagnosisRepository.findAllByPetIdOrderByCreatedAtAsc(petId);
        return DiagnosisTrendResponse.from(diagnoses);
    }

    // 기존 호환용
    @Transactional
    public Diagnosis createDiagnosis(Long petId, Long diseaseId, String imageUrl,
            EyePosition eyePosition, String rawSeverity, Double confidence) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Disease not found"));
        return diagnosisRepository.save(Diagnosis.fromRaw(pet, disease, imageUrl, eyePosition, rawSeverity, confidence));
    }

    @Transactional(readOnly = true)
    public DiagnosisDiseaseInfoResponse getDiseaseInfoByDiagnosisId(Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdWithDisease(diagnosisId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnosis not found"));
        Disease disease = diagnosis.getDisease();
        return new DiagnosisDiseaseInfoResponse(
                diagnosis.getId(), disease.getDiseaseId(),
                disease.getDiseaseName(), disease.getDiseaseImageUrl());
    }

    private void validatePetOwnership(Long userId, Long petId) {
        petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }
}
