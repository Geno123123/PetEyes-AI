package com.capstone.backend.repository;

import com.capstone.backend.entity.Hospital;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    Optional<Hospital> findByLatitudeAndLongitude(Double latitude, Double longitude);
}
