package com.wsantos.finance_backend.services.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginForm {
    @NotBlank @Email private String email;
    @NotBlank private String password;
}
