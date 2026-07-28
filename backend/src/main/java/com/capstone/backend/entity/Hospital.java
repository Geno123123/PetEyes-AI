package com.capstone.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import java.util.List;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hospitals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hospital {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "diagnosis_subject", length = 100)
    private String diagnosisSubject;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "operating_hours", length = 255)
    private String operatingHours;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "parking_available", nullable = false)
    @Builder.Default
    private boolean parkingAvailable = false;

    @Column(name = "night_care", nullable = false)
    @Builder.Default
    private boolean nightCare = false;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    public List<String> getKeywordList() {
        if (keywords == null || keywords.isBlank()) return List.of();
        return List.of(keywords.split(","));
    }

    public void setKeywordList(List<String> list) {
        this.keywords = (list == null || list.isEmpty()) ? null : String.join(",", list);
    }

    public double calculateDistanceKm(double userLatitude, double userLongitude) {
        double lat1 = Math.toRadians(latitude);
        double lon1 = Math.toRadians(longitude);
        double lat2 = Math.toRadians(userLatitude);
        double lon2 = Math.toRadians(userLongitude);

        double latDiff = lat2 - lat1;
        double lonDiff = lon2 - lon1;

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
