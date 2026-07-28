package com.capstone.backend.hospital.service;

import com.capstone.backend.config.NaverProperties;
import com.capstone.backend.entity.Hospital;
import com.capstone.backend.hospital.dto.NaverNearbyHospitalResponse;
import com.capstone.backend.hospital.util.NaverMapUrls;
import com.capstone.backend.repository.HospitalRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NaverHospitalService {

    // Naver 지역검색 API의 display 최대값은 5
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 5;
    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final double MAX_RADIUS_KM = 50.0;
    private static final double EARTH_RADIUS_KM = 6371.0;
    // 지역검색 API의 mapx/mapy는 WGS84 좌표를 1e7 배율로 곱한 정수
    private static final double NAVER_COORD_SCALE = 1e7;
    private static final String DEFAULT_QUERY = "동물병원";

    private final RestClient naverOpenApiRestClient;
    private final NaverProperties naverProperties;
    private final HospitalRepository hospitalRepository;

    @Transactional
    public List<NaverNearbyHospitalResponse> searchNearbyVeterinaryHospitals(
            Double latitude,
            Double longitude,
            Double radiusKm,
            Integer limit
    ) {
        validateCoordinates(latitude, longitude);
        validateApiKey();

        double effectiveRadiusKm = radiusKm == null ? DEFAULT_RADIUS_KM : radiusKm;
        int effectiveLimit = limit == null ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        validateRadius(effectiveRadiusKm);
        validateLimit(effectiveLimit);

        NaverLocalSearchResponse response;
        try {
            response = naverOpenApiRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/local.json")
                            .queryParam("query", DEFAULT_QUERY)
                            .queryParam("display", effectiveLimit)
                            .queryParam("start", 1)
                            .queryParam("sort", "random")
                            .build())
                    .header("X-Naver-Client-Id", naverProperties.clientId())
                    .header("X-Naver-Client-Secret", naverProperties.clientSecret())
                    .retrieve()
                    .body(NaverLocalSearchResponse.class);
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Naver search API request failed: " + ex.getStatusCode().value(),
                    ex
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to call Naver search API",
                    ex
            );
        }

        if (response == null || response.items() == null) {
            return List.of();
        }

        return response.items().stream()
                .map(item -> toResponse(item, latitude, longitude))
                .filter(item -> item.distanceKm() != null && item.distanceKm() <= effectiveRadiusKm)
                .sorted(Comparator.comparingDouble(NaverNearbyHospitalResponse::distanceKm))
                .map(this::upsertAndAttachId)
                .toList();
    }

    private NaverNearbyHospitalResponse upsertAndAttachId(NaverNearbyHospitalResponse response) {
        if (response.latitude() == null || response.longitude() == null) {
            return response;
        }
        Hospital hospital = hospitalRepository
                .findByLatitudeAndLongitude(response.latitude(), response.longitude())
                .orElseGet(() -> hospitalRepository.save(Hospital.builder()
                        .name(response.name())
                        .address(response.roadAddress() != null ? response.roadAddress() : response.address())
                        .phoneNumber(response.telephone())
                        .latitude(response.latitude())
                        .longitude(response.longitude())
                        .build()));
        return new NaverNearbyHospitalResponse(
                hospital.getHospitalId(),
                response.name(),
                response.category(),
                response.address(),
                response.roadAddress(),
                response.telephone(),
                response.latitude(),
                response.longitude(),
                response.distanceKm(),
                response.link(),
                response.naverMapUrl()
        );
    }

    private NaverNearbyHospitalResponse toResponse(NaverLocalItem item, double userLat, double userLng) {
        Double lat = parseCoord(item.mapy());
        Double lng = parseCoord(item.mapx());
        String name = stripHtmlTags(item.title());
        Double distance = (lat == null || lng == null) ? null : round(haversineKm(userLat, userLng, lat, lng), 2);
        return new NaverNearbyHospitalResponse(
                null,
                name,
                item.category(),
                emptyToNull(item.address()),
                emptyToNull(item.roadAddress()),
                emptyToNull(item.telephone()),
                lat == null ? null : round(lat, 6),
                lng == null ? null : round(lng, 6),
                distance,
                item.link(),
                NaverMapUrls.deeplink(name, lat, lng)
        );
    }

    private static Double parseCoord(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Long.parseLong(raw) / NAVER_COORD_SCALE;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String stripHtmlTags(String value) {
        return value == null ? null : value.replaceAll("<[^>]+>", "");
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    private static double round(double value, int digits) {
        double scale = Math.pow(10, digits);
        return Math.round(value * scale) / scale;
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Longitude must be between -180 and 180");
        }
    }

    private void validateRadius(double radiusKm) {
        if (radiusKm <= 0 || radiusKm > MAX_RADIUS_KM) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Radius must be between 0 and " + MAX_RADIUS_KM + " km"
            );
        }
    }

    private void validateLimit(int limit) {
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Limit must be between 1 and " + MAX_LIMIT);
        }
    }

    private void validateApiKey() {
        if (naverProperties.clientId() == null || naverProperties.clientId().isBlank()
                || naverProperties.clientSecret() == null || naverProperties.clientSecret().isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Naver search API key is missing");
        }
    }

    private record NaverLocalSearchResponse(
            List<NaverLocalItem> items
    ) {
    }

    private record NaverLocalItem(
            String title,
            String link,
            String category,
            String description,
            String telephone,
            String address,
            String roadAddress,
            String mapx,
            String mapy
    ) {
    }
}
