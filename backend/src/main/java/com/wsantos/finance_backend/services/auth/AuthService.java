package com.wsantos.finance_backend.services.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wsantos.finance_backend.services.auth.domain.AppUser;
import com.wsantos.finance_backend.services.auth.domain.LoginForm;
import com.wsantos.finance_backend.services.auth.domain.RegistrationForm;
import com.wsantos.finance_backend.services.auth.exceptions.EmailAlreadyRegistered;
import com.wsantos.finance_backend.services.auth.exceptions.LoginFailed;
import com.wsantos.finance_backend.services.currency.CurrencyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {
    private AuthRepository authRepository;
    private CurrencyRepository currencyRepository;
    private PasswordEncoder passwordEncoder;

    public AuthService(AuthRepository authRepository, CurrencyRepository currencyRepository, PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.currencyRepository = currencyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser registerUser(RegistrationForm form) {
        if (this.authRepository.findByEmailIgnoreCase(form.getEmail()).isPresent()) {
            log.info("Registration rejected: email already registered");
            throw new EmailAlreadyRegistered();
        }

        AppUser newUser = new AppUser(
            form.getEmail(),
            this.passwordEncoder.encode(form.getPassword()),
            this.currencyRepository.getReferenceById("USD")
        );

        AppUser saved;
        try {
            saved = this.authRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyRegistered();
        }
        log.info("Registered User: {}", saved.getId());
        return saved;
    }

    public AppUser loginUser(LoginForm form) {
        var optUser = this.authRepository.findByEmailIgnoreCase(form.getEmail());
        log.info("Logging in user: %s".formatted(form.getEmail()));

        if(optUser.isEmpty()) {
            log.info("Login failed: unknown email");
            throw new LoginFailed();
        }

        AppUser user = optUser.get();

        if (!passwordEncoder.matches(form.getPassword(), user.getPasswordHash())) {
            log.info("Login failed: wrong password for user {}", user.getId());
            throw new LoginFailed();
        }

        log.info("Logged in as user: %s".formatted(form.getEmail()));

        return user;
    }

    // Looks up a user by ID, empty when the user no longer exists.
    public Optional<AppUser> getUser(UUID id) {
        return this.authRepository.findById(id);
    }
}
