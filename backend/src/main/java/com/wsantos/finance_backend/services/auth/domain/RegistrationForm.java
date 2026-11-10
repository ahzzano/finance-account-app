package com.wsantos.finance_backend.services.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter 
public class RegistrationForm {
    @NotBlank @Email private String email;
    @NotBlank @Size(min=8) private String password;
}
