package com.capstone.backend.auth.service;

import com.capstone.backend.auth.dto.AuthCodeTokenRequest;
import com.capstone.backend.auth.dto.LoginRequest;
import com.capstone.backend.auth.dto.LoginTokenResponse;
import com.capstone.backend.auth.dto.SignupRequest;
import com.capstone.backend.auth.exception.DuplicateEmailException;
import com.capstone.backend.auth.jwt.JwtTokenProvider;
import com.capstone.backend.auth.service.SocialSignupSessionService.PendingSocialUser;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.type.AuthProvider;
import com.capstone.backend.entity.type.Role;
import com.capstone.backend.entity.type.VetVerificationStatus;
import com.capstone.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCodeService authCodeService;
    private final SocialSignupSessionService socialSignupSessionService;

    @Transactional
    public LoginTokenResponse signup(SignupRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String email = normalizeEmail(requireText(request.email(), "Email is required"));
        String password = requireText(request.password(), "Password is required");
        String name = requireText(request.name(), "Name is required");
        String nickname = normalizeOptionalText(request.nickname());
        String phoneNumber = normalizeOptionalText(request.phoneNumber());

        validatePassword(password);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        boolean requestedVet = request.role() == Role.ROLE_VET;
        Role role = Role.ROLE_USER;
        VetVerificationStatus verificationStatus = requestedVet
                ? VetVerificationStatus.PENDING
                : VetVerificationStatus.NONE;

        User user;
        try {
            user = userRepository.save(User.builder()
                    .providerId(AuthProvider.LOCAL.identity(email))
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .nickname(nickname)
                    .phoneNumber(phoneNumber)
                    .role(role)
                    .vetVerificationStatus(verificationStatus)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException();
        }

        return new LoginTokenResponse(jwtTokenProvider.createAccessToken(user));
    }

    @Transactional(readOnly = true)
    public LoginTokenResponse login(LoginRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String email = normalizeEmail(requireText(request.email(), "Email is required"));
        String password = requireText(request.password(), "Password is required");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return new LoginTokenResponse(jwtTokenProvider.createAccessToken(user));
    }

    @Transactional
    public LoginTokenResponse exchangeSocialToken(AuthCodeTokenRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        String code = normalizeOptionalText(request.code());
        if (code != null) {
            return exchangeWithLoginCode(code);
        }

        String signupCode = normalizeOptionalText(request.signupCode());
        if (signupCode != null) {
            return exchangeWithSignupCode(signupCode, request.name(), request.nickname(), request.phoneNumber());
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either code or signupCode is required");
    }

    private LoginTokenResponse exchangeWithLoginCode(String code) {
        User user = authCodeService.consume(code);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired code");
        }
        return new LoginTokenResponse(jwtTokenProvider.createAccessToken(user));
    }

    private LoginTokenResponse exchangeWithSignupCode(
            String signupCode,
            String requestName,
            String requestNickname,
            String requestPhoneNumber
    ) {
        PendingSocialUser pending = socialSignupSessionService.consume(signupCode);
        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired signupCode");
        }

        String provider = requireText(pending.provider(), "provider is missing").toUpperCase();
        AuthProvider authProvider = AuthProvider.valueOf(provider);
        String providerIdentity = authProvider.identity(requireText(pending.providerId(), "providerId is missing"));

        String email = normalizeEmail(requireText(pending.email(), "email is missing"));
        String name = normalizeOptionalText(requestName);
        if (name == null) {
            name = requireText(pending.name(), "name is missing");
        }
        String nickname = normalizeOptionalText(requestNickname);
        if (nickname == null) {
            nickname = normalizeOptionalText(pending.nickname());
        }
        String phoneNumber = normalizeOptionalText(requestPhoneNumber);

        User existingUserByEmail = userRepository.findByEmail(email).orElse(null);
        if (existingUserByEmail != null) {
            existingUserByEmail.setProviderId(providerIdentity);
            if (name != null && !name.isBlank()) {
                existingUserByEmail.setName(name);
            }
            if (nickname != null && !nickname.isBlank()) {
                existingUserByEmail.setNickname(nickname);
            }
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                existingUserByEmail.setPhoneNumber(phoneNumber);
            }
            if (pending.profileImageUrl() != null && !pending.profileImageUrl().isBlank()) {
                existingUserByEmail.setProfileImageUrl(pending.profileImageUrl());
            }
            User saved = userRepository.save(existingUserByEmail);
            return new LoginTokenResponse(jwtTokenProvider.createAccessToken(saved));
        }

        User newUser = userRepository.save(User.builder()
                .providerId(providerIdentity)
                .email(email)
                .name(name)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .profileImageUrl(pending.profileImageUrl())
                .role(Role.ROLE_USER)
                .vetVerificationStatus(VetVerificationStatus.NONE)
                .build());

        return new LoginTokenResponse(jwtTokenProvider.createAccessToken(newUser));
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
    }
}
