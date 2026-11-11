package com.wsantos.finance_backend.services.auth;

import java.util.UUID;

import com.wsantos.finance_backend.services.auth.domain.AppUser;

// Safe, public-facing view of an AppUser that excludes sensitive fields like the password hash.
public record UserResponse(UUID id, String email, String currencyCode) {
    // Builds a UserResponse from an AppUser entity.
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getCurrency().getCode());
    }
}
