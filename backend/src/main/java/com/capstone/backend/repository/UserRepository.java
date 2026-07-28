package com.capstone.backend.repository;

import com.capstone.backend.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByProviderId(String providerId);
}
