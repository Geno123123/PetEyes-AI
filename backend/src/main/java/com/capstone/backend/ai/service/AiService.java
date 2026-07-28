package com.capstone.backend.ai.service;

import com.capstone.backend.ai.client.AiServerClient;
import com.capstone.backend.ai.dto.AiDiagnoseResponse;
import com.capstone.backend.ai.dto.AiDiagnosisResult;
import com.capstone.backend.ai.dto.AiKeywordsResponse;
import com.capstone.backend.ai.dto.AiReportResponse;
import com.capstone.backend.diagnosis.dto.DiagnosisResponse;
import com.capstone.backend.entity.Diagnosis;
import com.capstone.backend.entity.Disease;
import com.capstone.backend.entity.Pet;
import com.capstone.backend.entity.Hospital;
import com.capstone.backend.entity.Review;
import com.capstone.backend.entity.type.EyePosition;
import com.capstone.backend.entity.type.Species;
import com.capstone.backend.repository.DiagnosisRepository;
import com.capstone.backend.repository.DiseaseRepository;
import com.capstone.backend.repository.HospitalRepository;
import com.capstone.backend.repository.PetRepository;
import com.capstone.backend.repository.ReviewRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final AiServerClient aiServerClient;
    private final PetRepository petRepository;
    private final DiseaseRepository diseaseRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final ReviewRepository reviewRepository;
    private final HospitalRepository hospitalRepository;

    @Transactional
    public AiDiagnosisResult diagnoseAndSave(
            Long userId, Long petId,
            MultipartFile image, String imageUrl,
            EyePosition eyePosition, String petType, String cropMode,
            int q1, int q2, int q3, int q4, int q5,
            int q6, int q7, int q8, int q9, int q10
    ) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
        String resolvedPetType = toPetType(pet.getSpecies());
        String resolvedCropMode = normalizeCropMode(cropMode);
        log.info("[AI] diagnoseAndSave 시작 - petId={}, userId={}, petSpecies={}, requestPetType={}",
                petId, userId, pet.getSpecies(), petType);

        AiDiagnoseResponse aiResult = aiServerClient.diagnose(
                image, resolvedPetType, resolvedCropMode,
                q1, q2, q3, q4, q5, q6, q7, q8, q9, q10
        );

        if (!aiResult.detected()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "안구 질환이 감지되지 않았습니다.");
        }

        log.info("[AI] 진단 결과 - disease={}, severityLabel={}, confidence={}, resolvedPetType={}, petSpecies={}",
                aiResult.disease(), aiResult.severityLabel(), aiResult.confidence(), resolvedPetType, pet.getSpecies());

        if (isNormalResult(aiResult)) {
            log.info("[AI] 정상 결과 처리 - disease={}, severityLabel={}", aiResult.disease(), aiResult.severityLabel());
            return buildAndSaveNormalResult(pet, imageUrl, eyePosition, aiResult);
        }

        Disease disease = diseaseRepository.findByDiseaseNameAndSpecies(aiResult.disease(), pet.getSpecies())
                .orElseThrow(() -> {
                    log.error("[AI] 질병 매핑 실패 - diseaseName={}, species={}", aiResult.disease(), pet.getSpecies());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "AI 결과 질병명을 찾을 수 없습니다: " + aiResult.disease() + " (" + pet.getSpecies() + ")");
                });

        Diagnosis saved = diagnosisRepository.save(
                Diagnosis.fromRaw(
                        pet,
                        disease,
                        imageUrl,
                        eyePosition,
                        aiResult.severityLabel(),
                        aiResult.confidence(),
                        aiResult.rgbHsvScore()
                )
        );

        AiReportResponse reportResponse = aiServerClient.generateReport(
                resolvedPetType, aiResult.disease(), saved.toSeverityLabel(), aiResult.score()
        );

        return new AiDiagnosisResult(
                DiagnosisResponse.from(saved),
                reportResponse.report(),
                aiResult.croppedImage()
        );
    }

    @Transactional(readOnly = true)
    public AiReportResponse getReport(Long userId, Long petId, Long diagnosisId) {
        petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        Diagnosis diagnosis = diagnosisRepository.findByIdAndPetId(diagnosisId, petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnosis not found"));

        String petType = toPetType(diagnosis.getPet().getSpecies());

        return aiServerClient.generateReport(
                petType,
                diagnosis.getDiseaseName(),
                diagnosis.toSeverityLabel(),
                diagnosis.getConfidence()
        );
    }

    private String toPetType(Species species) {
        return species == Species.CAT ? "cat" : "dog";
    }

    private String normalizeCropMode(String cropMode) {
        if (cropMode == null || cropMode.isBlank()) {
            return "YOLO";
        }
        String normalized = cropMode.trim().toUpperCase(Locale.ROOT);
        if (!"YOLO".equals(normalized) && !"USER".equals(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "crop_mode must be YOLO or USER");
        }
        return normalized;
    }

    private boolean isNormalResult(AiDiagnoseResponse aiResult) {
        return "정상".equals(aiResult.disease())
                || "정상 범위".equals(aiResult.severityLabel())
                || "정상범위".equals(aiResult.severityLabel());
    }

    private AiDiagnosisResult buildAndSaveNormalResult(
            Pet pet,
            String imageUrl,
            EyePosition eyePosition,
            AiDiagnoseResponse aiResult
    ) {
        Disease normalDisease = getOrCreateNormalDisease(pet.getSpecies());
        Diagnosis saved = diagnosisRepository.save(
                Diagnosis.fromRaw(
                        pet,
                        normalDisease,
                        imageUrl,
                        eyePosition,
                        "정상",
                        aiResult.confidence(),
                        aiResult.rgbHsvScore()
                )
        );

        return new AiDiagnosisResult(
                DiagnosisResponse.from(saved),
                aiResult.report(),
                aiResult.croppedImage()
        );
    }

    private Disease getOrCreateNormalDisease(Species species) {
        return diseaseRepository.findByDiseaseNameAndSpecies("정상", species)
                .orElseGet(() -> diseaseRepository.save(
                        Disease.builder()
                                .diseaseName("정상")
                                .species(species)
                                .diseaseImageUrl("")
                                .category("정상")
                                .description("안구 질환 징후가 감지되지 않은 정상 상태입니다.")
                                .symptoms("특이 증상 없음")
                                .careGuide("정기적으로 상태를 관찰하고 이상 시 재진단을 권장합니다.")
                                .build()
                ));
    }

    @Transactional
    public AiKeywordsResponse extractKeywords(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));

        List<String> reviewTexts = reviewRepository.findByHospitalId(hospitalId).stream()
                .map(Review::getReview)
                .toList();

        if (reviewTexts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "키워드 추출에 필요한 리뷰가 없습니다.");
        }

        AiKeywordsResponse result = aiServerClient.extractKeywords(hospitalId, reviewTexts);
        hospital.setKeywordList(result.keywords());
        hospitalRepository.save(hospital);
        return result;
    }
}
