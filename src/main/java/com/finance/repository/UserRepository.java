package com.finance.repository;

import com.finance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data auto-generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Used during registration to check duplicate emails
    boolean existsByEmail(String email);
}