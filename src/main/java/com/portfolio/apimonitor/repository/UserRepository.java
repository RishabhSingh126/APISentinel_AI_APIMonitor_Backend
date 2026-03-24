package com.portfolio.apimonitor.repository;

import com.portfolio.apimonitor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Boot magically writes the SQL to find a user by their email for login!
    Optional<User> findByEmail(String email);

    // Checks if an email is already taken during registration
    Boolean existsByEmail(String email);
}