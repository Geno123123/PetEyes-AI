package com.capstone.backend.hospital.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.hospital.dto.NearbyHospitalResponse;
import com.capstone.backend.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@Tag(name = "Hospitals", description = "병원 API")
@SecurityRequirement(name = "bearerAuth")
public class HospitalController implements HospitalControllerDocs {

    private final HospitalService hospitalService;

    @GetMapping("/nearby")
    @Override
    public List<NearbyHospitalResponse> getNearbyHospitals(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false, defaultValue = "false") boolean night,
            @RequestParam(required = false, defaultValue = "false") boolean sortByReviewCount
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return hospitalService.getNearbyHospitals(latitude, longitude, radiusKm, limit, night, sortByReviewCount);
    }
}
