package com.capstone.backend.pet.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.entity.Pet;
import com.capstone.backend.pet.dto.PetRequest;
import com.capstone.backend.pet.dto.PetResponse;
import com.capstone.backend.repository.PetRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "반려동물 API")
@SecurityRequirement(name = "bearerAuth")
public class PetController implements PetControllerDocs {

    private final PetRepository petRepository;

    @GetMapping
    @Override
    public List<PetResponse> getPets(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);
        return petRepository.findAllByUserIdOrderByIdDesc(userId).stream()
                .map(PetResponse::from)
                .toList();
    }

    @GetMapping("/{petId}")
    @Override
    public PetResponse getPet(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId
    ) {
        Pet pet = getOwnedPet(requireUserId(principal), petId);
        return PetResponse.from(pet);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public PetResponse createPet(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody PetRequest request
    ) {
        validateRequest(request);
        Long userId = requireUserId(principal);

        Pet pet = Pet.builder()
                .userId(userId)
                .name(request.name().trim())
                .breed(request.breed().trim())
                .species(request.species())
                .birthDate(request.birthDate())
                .build();

        return PetResponse.from(petRepository.save(pet));
    }

    @PutMapping("/{petId}")
    @Override
    public PetResponse updatePet(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId,
            @RequestBody PetRequest request
    ) {
        validateRequest(request);

        Pet pet = getOwnedPet(requireUserId(principal), petId);
        pet.updateInfo(
                request.name().trim(),
                request.breed().trim(),
                request.species(),
                request.birthDate()
        );

        return PetResponse.from(petRepository.save(pet));
    }

    @DeleteMapping("/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void deletePet(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long petId
    ) {
        Pet pet = getOwnedPet(requireUserId(principal), petId);
        petRepository.delete(pet);
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return principal.id();
    }

    private Pet getOwnedPet(Long userId, Long petId) {
        return petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validateRequest(PetRequest request) {
        if (request == null
                || request.name() == null || request.name().isBlank()
                || request.breed() == null || request.breed().isBlank()
                || request.species() == null
                || request.birthDate() == null
                || request.birthDate().isAfter(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pet request");
        }
    }
}
