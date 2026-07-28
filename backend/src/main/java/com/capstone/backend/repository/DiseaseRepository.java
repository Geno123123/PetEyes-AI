package com.capstone.backend.repository;

import com.capstone.backend.entity.Disease;
import com.capstone.backend.entity.type.Species;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    Optional<Disease> findByDiseaseNameAndSpecies(String diseaseName, Species species);
}
