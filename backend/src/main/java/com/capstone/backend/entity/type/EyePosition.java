package com.capstone.backend.entity.type;

public enum EyePosition {
    LEFT,
    RIGHT;

    public static EyePosition fromString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("eyePosition is null");
        }

        return switch (raw.trim().toUpperCase()) {
            case "LEFT", "좌", "좌안" -> LEFT;
            case "RIGHT", "우", "우안" -> RIGHT;
            default -> throw new IllegalArgumentException("Unknown eye position: " + raw);
        };
    }
}
