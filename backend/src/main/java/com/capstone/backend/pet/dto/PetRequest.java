package com.capstone.backend.pet.dto;

import com.capstone.backend.entity.type.Species;
import java.time.LocalDate;

public record PetRequest(
        String name,
        String breed,
        Species species,
        LocalDate birthDate
) {
}
