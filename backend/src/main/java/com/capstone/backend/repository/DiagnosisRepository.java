package com.capstone.backend.repository;

import com.capstone.backend.entity.Diagnosis;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    @Query("SELECT d FROM Diagnosis d JOIN FETCH d.disease WHERE d.id = :diagnosisId")
    Optional<Diagnosis> findByIdWithDisease(@Param("diagnosisId") Long diagnosisId);

    @Query("SELECT d FROM Diagnosis d JOIN FETCH d.disease WHERE d.pet.id = :petId ORDER BY d.createdAt DESC")
    List<Diagnosis> findAllByPetIdWithDisease(@Param("petId") Long petId);

    @Query("SELECT d FROM Diagnosis d JOIN FETCH d.disease WHERE d.pet.id = :petId ORDER BY d.createdAt ASC")
    List<Diagnosis> findAllByPetIdOrderByCreatedAtAsc(@Param("petId") Long petId);

    @Query("SELECT d FROM Diagnosis d JOIN FETCH d.disease WHERE d.id = :diagnosisId AND d.pet.id = :petId")
    Optional<Diagnosis> findByIdAndPetId(@Param("diagnosisId") Long diagnosisId, @Param("petId") Long petId);

    boolean existsByIdAndPetUserId(Long id, Long userId);
}
