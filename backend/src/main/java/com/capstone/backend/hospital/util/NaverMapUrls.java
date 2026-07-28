package com.capstone.backend.hospital.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class NaverMapUrls {

    private NaverMapUrls() {
    }

    public static String deeplink(String name, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        String encoded = URLEncoder.encode(name == null ? "" : name, StandardCharsets.UTF_8);
        return String.format(
                "https://map.naver.com/p/search/%s?c=%.6f,%.6f,15,0,0,0,dh",
                encoded, longitude, latitude
        );
    }
}
