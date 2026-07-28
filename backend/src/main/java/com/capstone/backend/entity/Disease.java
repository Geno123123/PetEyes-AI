package com.capstone.backend.entity;

import com.capstone.backend.entity.type.Species;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "diseases",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_diseases_name_species", columnNames = {"disease_name", "species"})
        }
)
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "disease_id")
    private Long diseaseId;

    @Column(name = "disease_name", nullable = false, length = 100)
    private String diseaseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(name = "disease_image_url", length = 500)
    private String diseaseImageUrl;

    @Column(nullable = false, length = 100)
    private String category;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 500)
    private String symptoms;

    @Column(length = 500)
    private String cause;

    @Column(name = "care_guide", length = 1000)
    private String careGuide;

    @Column(length = 1000)
    private String prevention;

    @Column(length = 1000)
    private String treatment;

    @Column(name = "related_diseases", length = 500)
    private String relatedDiseases;

    // 중증도별 예시 사진
    @Column(name = "mild_example_image_url", length = 500)
    private String mildExampleImageUrl;

    @Column(name = "caution_example_image_url", length = 500)
    private String cautionExampleImageUrl;

    @Column(name = "danger_example_image_url", length = 500)
    private String dangerExampleImageUrl;
}
