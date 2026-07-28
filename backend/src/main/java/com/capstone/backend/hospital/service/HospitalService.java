package com.capstone.backend.hospital.service;

import com.capstone.backend.entity.Hospital;
import com.capstone.backend.hospital.dto.NearbyHospitalResponse;
import com.capstone.backend.repository.HospitalRepository;
import com.capstone.backend.repository.ReviewRepository;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int NIGHT_START_HOUR = 20;
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*[-~]\\s*(\\d{1,2}):(\\d{2})");

    private final HospitalRepository hospitalRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<NearbyHospitalResponse> getNearbyHospitals(
            Double latitude,
            Double longitude,
            Double radiusKm,
            Integer limit,
            boolean night,
            boolean sortByReviewCount
    ) {
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }

        double effectiveRadiusKm = radiusKm == null ? DEFAULT_RADIUS_KM : radiusKm;
        int effectiveLimit = limit == null ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        List<HospitalDistance> nearby = hospitalRepository.findAll().stream()
                .map(h -> new HospitalDistance(h, h.calculateDistanceKm(latitude, longitude)))
                .filter(hd -> hd.distanceKm() <= effectiveRadiusKm)
                .filter(hd -> !night || hasNightCareByOperatingHours(hd.hospital().getOperatingHours()))
                .toList();

        if (nearby.isEmpty()) {
            return List.of();
        }

        List<Long> ids = nearby.stream().map(hd -> hd.hospital().getHospitalId()).toList();
        Map<Long, Double> avgRatings = reviewRepository.findAverageRatingsByHospitalIds(ids).stream()
                .collect(Collectors.toMap(
                        ReviewRepository.HospitalAverageRatingProjection::getHospitalId,
                        ReviewRepository.HospitalAverageRatingProjection::getAverageRating
                ));
        Map<Long, Long> reviewCounts = reviewRepository.findReviewCountsByHospitalIds(ids).stream()
                .collect(Collectors.toMap(
                        ReviewRepository.HospitalReviewCountProjection::getHospitalId,
                        ReviewRepository.HospitalReviewCountProjection::getReviewCount
                ));

        Comparator<HospitalDistance> sortComparator = Comparator.comparingDouble(HospitalDistance::distanceKm);
        if (sortByReviewCount) {
            sortComparator = Comparator
                    .comparingLong((HospitalDistance hd) -> reviewCounts.getOrDefault(hd.hospital().getHospitalId(), 0L))
                    .reversed()
                    .thenComparingDouble(HospitalDistance::distanceKm);
        }

        return nearby.stream()
                .sorted(sortComparator)
                .limit(effectiveLimit)
                .map(hd -> NearbyHospitalResponse.from(
                        hd.hospital(),
                        round(hd.distanceKm()),
                        round(avgRatings.getOrDefault(hd.hospital().getHospitalId(), 0.0))
                ))
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean hasNightCareByOperatingHours(String operatingHours) {
        if (operatingHours == null || operatingHours.isBlank()) {
            return false;
        }

        Matcher matcher = TIME_RANGE_PATTERN.matcher(operatingHours);
        while (matcher.find()) {
            int startHour = Integer.parseInt(matcher.group(1));
            int startMinute = Integer.parseInt(matcher.group(2));
            int endHour = Integer.parseInt(matcher.group(3));
            int endMinute = Integer.parseInt(matcher.group(4));

            if (!isValidTime(startHour, startMinute) || !isValidTime(endHour, endMinute)) {
                continue;
            }

            LocalTime start = LocalTime.of(startHour, startMinute);
            LocalTime end = LocalTime.of(endHour, endMinute);

            if (!end.isAfter(start)) {
                return true;
            }
            if (end.getHour() >= NIGHT_START_HOUR) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidTime(int hour, int minute) {
        return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
    }

    private record HospitalDistance(Hospital hospital, double distanceKm) {}
}
