package com.capstone.backend.entity.type;

public enum NonUlcerativeType {
    UPPER,
    LOWER,
    NONE;

    public static NonUlcerativeType fromKorean(String raw) {
        return switch (raw) {
            case "상", "중증" -> UPPER;
            case "하", "경증" -> LOWER;
            case "무", "정상", "정상 범위", "정상범위", "없음" -> NONE;
            default -> throw new IllegalArgumentException("Unknown non-ulcerative type: " + raw);
        };
    }
}
