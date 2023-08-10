package com.example.authserver.repository;

import com.example.authserver.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository  extends JpaRepository<UserCredential,Integer> {
    Optional<UserCredential> findByName(String username);
    boolean existsByNameAndEmail(String username, String email);
}
