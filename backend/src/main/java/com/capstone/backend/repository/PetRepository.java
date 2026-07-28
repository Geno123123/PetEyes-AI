package com.capstone.backend.repository;

import com.capstone.backend.entity.Pet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByUserIdOrderByIdDesc(Long userId);

    Optional<Pet> findByIdAndUserId(Long id, Long userId);
}
