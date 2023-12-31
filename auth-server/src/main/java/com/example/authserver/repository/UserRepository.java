package com.example.authserver.repository;

import com.example.authserver.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByName(String username);

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);
}
