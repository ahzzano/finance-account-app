package com.wsantos.finance_backend.services.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wsantos.finance_backend.services.auth.domain.AppUser;

public interface AuthRepository extends JpaRepository<AppUser, UUID>{
    Optional<AppUser> findByEmailIgnoreCase(String email);
}
