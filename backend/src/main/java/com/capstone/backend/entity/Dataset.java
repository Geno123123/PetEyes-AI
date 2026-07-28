package com.capstone.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String s3Url;      // S3 이미지 주소
    private String diseaseName; // 질병명 (안검염, 백내장 등)

    /**
     * [핵심] 모든 Enum의 결과값을 문자열로 통합 저장합니다.
     * 예: "POSITIVE", "UPPER", "IMMATURE" 등
     */
    private String resultValue;
}