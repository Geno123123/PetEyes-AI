package com.capstone.backend.diagnosis.dto;

import com.capstone.backend.entity.Diagnosis;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(description = "진단 추이 응답")
public record DiagnosisTrendResponse(
        @Schema(description = "총 진단 횟수") int totalCount,
        @Schema(description = "조회 기간", example = "2025.11 ~ 2026.03") String period,
        @Schema(description = "평균 정확도") double accuracyAverage,
        @Schema(description = "주요 질병명") String diseaseName,
        @Schema(description = "추이 데이터") List<TrendItem> trend
) {
    public record TrendItem(
            @Schema(description = "진단 ID") Long diagnosisId,
            @Schema(description = "진단 날짜", example = "2025-11-12") String date,
            @Schema(description = "중증도 레벨", example = "NORMAL / CAUTION / DANGER") String severityLevel,
            @Schema(description = "중증도 라벨", example = "정상 / 관찰 필요 / 진료 권장") String severityLabel,
            @Schema(description = "정확도") Double confidence,
            @Schema(description = "RGB/HSV 기반 점수") Double rgbHsvScore
    ) {
        private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        public static TrendItem from(Diagnosis d) {
            return new TrendItem(
                    d.getId(),
                    d.getCreatedAt().format(DATE_FMT),
                    d.toSeverityLevel(),
                    d.toSeverityLabel(),
                    d.getConfidence(),
                    d.getRgbHsvScore()
            );
        }
    }

    public static DiagnosisTrendResponse from(List<Diagnosis> diagnoses) {
        if (diagnoses.isEmpty()) {
            return new DiagnosisTrendResponse(0, "-", 0.0, "-", List.of());
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM");
        String period = diagnoses.get(0).getCreatedAt().format(fmt)
                + " ~ " + diagnoses.get(diagnoses.size() - 1).getCreatedAt().format(fmt);

        double avgAccuracy = diagnoses.stream()
                .mapToDouble(Diagnosis::getConfidence)
                .average()
                .orElse(0.0);

        // 가장 최근 진단의 질병명
        String diseaseName = diagnoses.get(diagnoses.size() - 1).getDiseaseName();

        List<TrendItem> trend = diagnoses.stream()
                .map(TrendItem::from)
                .toList();

        return new DiagnosisTrendResponse(
                diagnoses.size(),
                period,
                Math.round(avgAccuracy * 10.0) / 10.0,
                diseaseName,
                trend
        );
    }
}
