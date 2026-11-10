package com.wsantos.finance_backend.services.auth;

import java.util.Optional;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wsantos.finance_backend.services.auth.domain.AppUser;
import com.wsantos.finance_backend.services.auth.domain.LoginForm;
import com.wsantos.finance_backend.services.auth.domain.RegistrationForm;
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
        AppUser newUser = new AppUser(
            form.getEmail(),
            this.passwordEncoder.encode(form.getPassword()),
            this.currencyRepository.getReferenceById("USD")
        );
        AppUser saved = this.authRepository.save(newUser);
        log.info("Registered User: {}", saved.getId());
        return saved;
    }

    public AppUser loginUser(LoginForm form) throws LoginException {
        var optUser = this.authRepository.findByEmailIgnoreCase(form.getEmail());
        log.info("Logging in user: %s".formatted(form.getEmail()));

        if(optUser.isEmpty()) {
            throw new LoginException("User not found");
        }

        AppUser user = optUser.get();

        if (!passwordEncoder.matches(form.getPassword(), user.getPasswordHash())) {
            throw new LoginException("Password does not match");
        }

        log.info("Logged in as user: %s".formatted(form.getEmail()));

        return user;
    }

    // Looks up a user by ID, empty when the user no longer exists.
    public Optional<AppUser> getUser(UUID id) {
        return this.authRepository.findById(id);
    }
}
