package com.capstone.backend.admin.controller;

import com.capstone.backend.admin.dto.*;
import com.capstone.backend.admin.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 API")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public StatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/stats/dashboard")
    public AdminDashboardResponse getDashboardStats() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getUsers() {
        return adminService.getUsers();
    }

    @PatchMapping("/users/{id}/role")
    public AdminUserResponse updateUserRole(@PathVariable Long id, @RequestBody AdminRoleUpdateRequest request) {
        return adminService.updateUserRole(id, request);
    }

    @PatchMapping("/users/{id}/vet-verification")
    public AdminUserResponse updateVetVerificationStatus(
            @PathVariable Long id,
            @RequestBody AdminVetVerificationUpdateRequest request
    ) {
        return adminService.updateVetVerificationStatus(id, request);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hospitals")
    public List<AdminHospitalResponse> getHospitals() {
        return adminService.getHospitals();
    }

    @PostMapping("/hospitals")
    public ResponseEntity<AdminHospitalResponse> createHospital(@RequestBody AdminHospitalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createHospital(request));
    }

    @PutMapping("/hospitals/{id}")
    public AdminHospitalResponse updateHospital(@PathVariable Long id, @RequestBody AdminHospitalRequest request) {
        return adminService.updateHospital(id, request);
    }

    @DeleteMapping("/hospitals/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        adminService.deleteHospital(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews")
    public List<AdminReviewResponse> getReviews() {
        return adminService.getReviews();
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/qna")
    public List<AdminQnaResponse> getQnaPosts() {
        return adminService.getQnaPosts();
    }

    @DeleteMapping("/qna/{id}")
    public ResponseEntity<Void> deleteQnaPost(@PathVariable Long id) {
        adminService.deleteQnaPost(id);
        return ResponseEntity.noContent().build();
    }
}
