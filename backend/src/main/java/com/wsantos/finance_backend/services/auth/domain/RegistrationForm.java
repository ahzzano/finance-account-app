package com.wsantos.finance_backend.services.auth.domain;

import java.nio.charset.StandardCharsets;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter 
public class RegistrationForm {
    @NotBlank @Email private String email;
    @NotBlank @Size(min=8) private String password;

    // BCrypt rejects passwords longer than 72 bytes.
    @AssertTrue(message = "Password must be at most 72 bytes.")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
