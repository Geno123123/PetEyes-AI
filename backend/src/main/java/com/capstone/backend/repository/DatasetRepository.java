package com.capstone.backend.repository;

import com.capstone.backend.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    // 질병 이름으로 대표 사진 하나를 가져오는 메서드
    Optional<Dataset> findFirstByDiseaseName(String diseaseName);
}