package com.capstone.backend.entity.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Locale;

public enum Species {
    DOG,
    CAT;

    @JsonCreator
    public static Species from(String raw) {
        return Species.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
