package com.capstone.backend.pet.dto;

import com.capstone.backend.entity.Pet;
import com.capstone.backend.entity.type.Species;
import java.time.LocalDate;

public record PetResponse(
        Long id,
        String name,
        String breed,
        Species species,
        LocalDate birthDate,
        Integer age
) {

    public static PetResponse from(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getBreed(),
                pet.getSpecies(),
                pet.getBirthDate(),
                pet.calculateAge()
        );
    }
}
