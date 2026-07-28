package com.capstone.backend.entity.type;

import java.util.Locale;

public enum AuthProvider {
    LOCAL,
    NAVER,
    KAKAO;

    public String identity(String rawProviderId) {
        return code() + ":" + rawProviderId;
    }

    public String code() {
        return name().toLowerCase(Locale.ROOT);
    }
}
