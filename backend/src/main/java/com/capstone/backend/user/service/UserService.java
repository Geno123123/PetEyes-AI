package com.capstone.backend.user.service;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.type.Role;
import com.capstone.backend.entity.type.VetVerificationStatus;
import com.capstone.backend.repository.ReviewRepository;
import com.capstone.backend.repository.UserRepository;
import com.capstone.backend.user.dto.ChangePasswordRequest;
import com.capstone.backend.user.dto.UpdateUserRequest;
import com.capstone.backend.user.dto.UserMeResponse;
import com.capstone.backend.user.dto.VetVerificationRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserMeResponse getMe(UserPrincipal principal) {
        User user = getCurrentUser(principal);
        return UserMeResponse.from(user);
    }

    @Transactional
    public UserMeResponse updateMe(UserPrincipal principal, UpdateUserRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }

        User user = getCurrentUser(principal);
        user.setName(request.name().trim());

        if (request.nickname() != null) {
            user.setNickname(request.nickname().isBlank() ? null : request.nickname().trim());
        }
        if (request.profileImageUrl() != null) {
            user.setProfileImageUrl(request.profileImageUrl().isBlank() ? null : request.profileImageUrl().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }

        return UserMeResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        if (request == null || request.newPassword() == null || request.newPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }

        if (request.newPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters");
        }

        User user = getCurrentUser(principal);
        String encodedPassword = user.getPassword();

        if (encodedPassword != null && !encodedPassword.isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is required");
            }
            if (!passwordEncoder.matches(request.currentPassword(), encodedPassword)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
            }
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteMe(UserPrincipal principal) {
        User user = getCurrentUser(principal);
        reviewRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    @Transactional
    public UserMeResponse requestVetVerification(UserPrincipal principal, VetVerificationRequest request) {
        if (request == null || request.licenseNumber() == null || request.licenseNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "License number is required");
        }

        User user = getCurrentUser(principal);
        if (user.getVetVerificationStatus() == VetVerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Verification already pending");
        }

        user.setRole(Role.ROLE_USER);
        user.setVetVerificationStatus(VetVerificationStatus.PENDING);
        user.setVetLicenseNumber(request.licenseNumber().trim());
        user.setVetProofImageUrl(request.proofImageUrl() == null ? null : request.proofImageUrl().trim());
        user.setVetVerificationRequestedAt(LocalDateTime.now());
        user.setVetVerificationReviewedAt(null);
        user.setVetVerificationReviewNote(null);

        return UserMeResponse.from(userRepository.save(user));
    }

    private User getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
