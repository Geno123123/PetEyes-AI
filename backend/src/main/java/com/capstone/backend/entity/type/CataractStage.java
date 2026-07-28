package com.capstone.backend.entity.type;

public enum CataractStage {
    INCIPIENT,
    IMMATURE,
    MATURE,
    NORMAL;

    public static CataractStage fromKorean(String raw) {
        return switch (raw) {
            case "초기" -> INCIPIENT;
            case "비성숙", "경증" -> IMMATURE;
            case "성숙", "중증" -> MATURE;
            case "무", "정상", "없음" -> NORMAL;
            default -> throw new IllegalArgumentException("Unknown cataract stage: " + raw);
        };
    }
}
