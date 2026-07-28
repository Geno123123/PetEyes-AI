package com.capstone.backend.entity;

import com.capstone.backend.entity.type.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "eye_position", nullable = false, length = 10)
    private EyePosition eyePosition;

    @Column(name = "disease_name", nullable = false, length = 100)
    private String diseaseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "cataract", length = 20)
    private CataractStage cataract;

    @Enumerated(EnumType.STRING)
    @Column(name = "non_ulcerative_type", length = 20)
    private NonUlcerativeType nonUlcerativeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "disease_presence", length = 20)
    private DiseasePresence diseasePresence;

    @Column(name = "severity", nullable = false, length = 50)
    private String severity;

    @Column(nullable = false)
    private Double confidence;

    @Column(name = "rgb_hsv_score")
    private Double rgbHsvScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static Diagnosis fromRaw(
            Pet pet,
            Disease disease,
            String imageUrl,
            EyePosition eyePosition,
            String rawSeverity,
            Double confidence
    ) {
        return fromRaw(pet, disease, imageUrl, eyePosition, rawSeverity, confidence, null);
    }

    public static Diagnosis fromRaw(
            Pet pet,
            Disease disease,
            String imageUrl,
            EyePosition eyePosition,
            String rawSeverity,
            Double confidence,
            Double rgbHsvScore
    ) {
        String diseaseName = disease.getDiseaseName();
        Enum<?> mappedSeverity = mapSeverity(diseaseName, rawSeverity);

        return Diagnosis.builder()
                .pet(pet)
                .disease(disease)
                .imageUrl(imageUrl)
                .eyePosition(eyePosition)
                .diseaseName(diseaseName)
                .severity(mappedSeverity.name())
                .cataract(mappedSeverity instanceof CataractStage ? (CataractStage) mappedSeverity : null)
                .nonUlcerativeType(mappedSeverity instanceof NonUlcerativeType ? (NonUlcerativeType) mappedSeverity : null)
                .diseasePresence(mappedSeverity instanceof DiseasePresence ? (DiseasePresence) mappedSeverity : null)
                .confidence(confidence)
                .rgbHsvScore(rgbHsvScore)
                .build();
    }

    private static Enum<?> mapSeverity(String diseaseName, String rawSeverity) {
        Double numericSeverity = parseNumericSeverity(rawSeverity);
        if (numericSeverity != null) {
            return mapNumericSeverity(diseaseName, numericSeverity);
        }

        if (isCataract(diseaseName)) {
            return CataractStage.fromKorean(rawSeverity);
        }
        if (isNonUlcerativeCornealDisease(diseaseName)) {
            return NonUlcerativeType.fromKorean(rawSeverity);
        }
        return DiseasePresence.fromKorean(rawSeverity);
    }

    private static Enum<?> mapNumericSeverity(String diseaseName, double numericSeverity) {
        if (isCataract(diseaseName)) {
            if (numericSeverity < 0.25) return CataractStage.NORMAL;
            if (numericSeverity < 0.5) return CataractStage.INCIPIENT;
            if (numericSeverity < 0.75) return CataractStage.IMMATURE;
            return CataractStage.MATURE;
        }

        if (isNonUlcerativeCornealDisease(diseaseName)) {
            if (numericSeverity < 0.34) return NonUlcerativeType.NONE;
            if (numericSeverity < 0.67) return NonUlcerativeType.LOWER;
            return NonUlcerativeType.UPPER;
        }

        return numericSeverity >= 0.5 ? DiseasePresence.POSITIVE : DiseasePresence.NEGATIVE;
    }

    private static Double parseNumericSeverity(String rawSeverity) {
        if (rawSeverity == null) {
            return null;
        }

        String normalized = rawSeverity.trim().replace(",", ".");
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            double value = Double.parseDouble(normalized);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return clamp01(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static boolean isCataract(String diseaseName) {
        return "백내장".equals(diseaseName) || "CATARACT".equalsIgnoreCase(diseaseName);
    }

    private static boolean isNonUlcerativeCornealDisease(String diseaseName) {
        return "비궤양성각막질환".equals(diseaseName)
                || "NON_ULCERATIVE".equalsIgnoreCase(diseaseName)
                || "NON_ULCERATIVE_CORNEAL".equalsIgnoreCase(diseaseName);
    }

    /** 추이 화면용 단순화 중증도: NORMAL / CAUTION / DANGER */
    public String toSeverityLevel() {
        return switch (severity) {
            case "MATURE", "POSITIVE" -> "DANGER";
            case "INCIPIENT", "IMMATURE", "UPPER", "LOWER" -> "CAUTION";
            default -> "NORMAL";
        };
    }

    public String toSeverityLabel() {
        return switch (toSeverityLevel()) {
            case "DANGER" -> "진료 권장";
            case "CAUTION" -> "관찰 필요";
            default -> "정상";
        };
    }
}
