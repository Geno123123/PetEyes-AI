package com.capstone.backend.admin.service;

import com.capstone.backend.admin.dto.*;
import com.capstone.backend.entity.Hospital;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.type.Role;
import com.capstone.backend.entity.type.VetVerificationStatus;
import com.capstone.backend.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final ReviewRepository reviewRepository;
    private final QnaPostRepository qnaPostRepository;
    private final DiagnosisRepository diagnosisRepository;

    public StatsResponse getStats() {
        return new StatsResponse(
                userRepository.count(),
                hospitalRepository.count(),
                diagnosisRepository.count(),
                reviewRepository.count(),
                qnaPostRepository.count()
        );
    }

    public AdminDashboardResponse getDashboardStats() {
        StatsResponse summary = getStats();

        Map<LocalDate, long[]> daily = new TreeMap<>();
        long usersWithoutCreatedAt = 0L;

        for (User user : userRepository.findAll()) {
            if (user.getCreatedAt() == null) {
                usersWithoutCreatedAt++;
                continue;
            }
            LocalDate date = user.getCreatedAt().toLocalDate();
            daily.computeIfAbsent(date, k -> new long[4])[0]++;
        }

        diagnosisRepository.findAll().forEach(d -> {
            LocalDate date = d.getCreatedAt().toLocalDate();
            daily.computeIfAbsent(date, k -> new long[4])[1]++;
        });
        reviewRepository.findAll().forEach(r -> {
            LocalDate date = r.getCreatedAt().toLocalDate();
            daily.computeIfAbsent(date, k -> new long[4])[2]++;
        });
        qnaPostRepository.findAll().forEach(q -> {
            LocalDate date = q.getCreatedAt().toLocalDate();
            daily.computeIfAbsent(date, k -> new long[4])[3]++;
        });

        long cumulativeUsers = usersWithoutCreatedAt;
        List<AdminDashboardResponse.DailyMetric> dailyMetrics = new java.util.ArrayList<>();
        for (Map.Entry<LocalDate, long[]> entry : daily.entrySet()) {
            long[] values = entry.getValue();
            cumulativeUsers += values[0];
            dailyMetrics.add(new AdminDashboardResponse.DailyMetric(
                    entry.getKey().toString(),
                    values[0],
                    cumulativeUsers,
                    values[1],
                    values[2],
                    values[3]
            ));
        }

        List<AdminDashboardResponse.CategoryShare> diagnosisCategoryShares = diagnosisRepository.findAll().stream()
                .collect(Collectors.groupingBy(d -> d.getDiseaseName() == null ? "미분류" : d.getDiseaseName(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new AdminDashboardResponse.CategoryShare(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .toList();

        List<AdminDashboardResponse.CategoryShare> qnaSpeciesShares = qnaPostRepository.findAll().stream()
                .collect(Collectors.groupingBy(q -> q.getSpecies() == null ? "미지정" : q.getSpecies().name(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new AdminDashboardResponse.CategoryShare(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .toList();

        return new AdminDashboardResponse(summary, dailyMetrics, diagnosisCategoryShares, qnaSpeciesShares);
    }

    public List<AdminUserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUserRole(Long id, AdminRoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Role newRole;
        try {
            newRole = Role.valueOf(request.role());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        user.setRole(newRole);
        if (newRole == Role.ROLE_VET && user.getVetVerificationStatus() != VetVerificationStatus.APPROVED) {
            user.setVetVerificationStatus(VetVerificationStatus.APPROVED);
            user.setVetVerificationReviewedAt(LocalDateTime.now());
        }
        if (newRole != Role.ROLE_VET && user.getVetVerificationStatus() == VetVerificationStatus.APPROVED) {
            user.setVetVerificationStatus(VetVerificationStatus.REJECTED);
            user.setVetVerificationReviewedAt(LocalDateTime.now());
        }
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse updateVetVerificationStatus(Long id, AdminVetVerificationUpdateRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        VetVerificationStatus status;
        try {
            status = VetVerificationStatus.valueOf(request.status());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification status");
        }

        user.setVetVerificationStatus(status);
        user.setVetVerificationReviewedAt(LocalDateTime.now());
        user.setVetVerificationReviewNote(request.reviewNote() == null ? null : request.reviewNote().trim());

        if (status == VetVerificationStatus.APPROVED) {
            user.setRole(Role.ROLE_VET);
        } else if (user.getRole() == Role.ROLE_VET) {
            user.setRole(Role.ROLE_USER);
        }

        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    public List<AdminHospitalResponse> getHospitals() {
        return hospitalRepository.findAll().stream()
                .map(AdminHospitalResponse::from)
                .toList();
    }

    @Transactional
    public AdminHospitalResponse createHospital(AdminHospitalRequest request) {
        Hospital saved = hospitalRepository.save(Hospital.builder()
                .name(request.name())
                .address(request.address())
                .phoneNumber(request.phoneNumber())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .parkingAvailable(request.parkingAvailable())
                .nightCare(request.nightCare())
                .build());
        return AdminHospitalResponse.from(saved);
    }

    @Transactional
    public AdminHospitalResponse updateHospital(Long id, AdminHospitalRequest request) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found"));
        hospital.setName(request.name());
        hospital.setAddress(request.address());
        hospital.setPhoneNumber(request.phoneNumber());
        hospital.setLatitude(request.latitude());
        hospital.setLongitude(request.longitude());
        hospital.setParkingAvailable(request.parkingAvailable());
        hospital.setNightCare(request.nightCare());
        return AdminHospitalResponse.from(hospital);
    }

    @Transactional
    public void deleteHospital(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital not found");
        }
        hospitalRepository.deleteById(id);
    }

    public List<AdminReviewResponse> getReviews() {
        List<com.capstone.backend.entity.Review> reviews = reviewRepository.findAll();
        List<Long> hospitalIds = reviews.stream().map(com.capstone.backend.entity.Review::getHospitalId).distinct().toList();
        Map<Long, String> hospitalNames = hospitalRepository.findAllById(hospitalIds).stream()
                .collect(Collectors.toMap(Hospital::getHospitalId, Hospital::getName));
        return reviews.stream()
                .map(r -> AdminReviewResponse.from(r, hospitalNames.getOrDefault(r.getHospitalId(), "알 수 없음")))
                .toList();
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
        reviewRepository.deleteById(id);
    }

    public List<AdminQnaResponse> getQnaPosts() {
        return qnaPostRepository.findAll().stream()
                .map(AdminQnaResponse::from)
                .toList();
    }

    @Transactional
    public void deleteQnaPost(Long id) {
        if (!qnaPostRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "QnA post not found");
        }
        qnaPostRepository.deleteById(id);
    }
}
