package com.capstone.backend.entity.type;

public enum DiseasePresence {
    POSITIVE,
    NEGATIVE;

    public static DiseasePresence fromKorean(String raw) {
        return switch (raw) {
            case "유", "중증", "경증", "위험" -> POSITIVE;
            case "무", "정상", "정상 범위", "정상범위", "없음" -> NEGATIVE;
            default -> throw new IllegalArgumentException("Unknown disease presence: " + raw);
        };
    }
}
