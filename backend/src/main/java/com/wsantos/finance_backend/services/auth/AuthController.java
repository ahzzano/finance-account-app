package com.wsantos.finance_backend.services.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsantos.finance_backend.services.auth.domain.AppUser;
import com.wsantos.finance_backend.services.auth.domain.LoginForm;
import com.wsantos.finance_backend.services.auth.domain.RegistrationForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;


@Slf4j 
@RequestMapping("/auth")
@RestController 
public class AuthController {
    private AuthService service;
    private SecurityContextRepository securityContextRepository;

    public AuthController(AuthService service, SecurityContextRepository securityContextRepository) {
        this.service = service;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody @Valid RegistrationForm form) {
        return UserResponse.from(this.service.registerUser(form));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse loginUser(@RequestBody @Valid LoginForm loginForm, HttpSession session, HttpServletRequest request, HttpServletResponse response) {

        AppUser user = this.service.loginUser(loginForm);

        // Rotate the session ID to prevent session fixation.
        request.changeSessionId();
        log.debug("Rotated session id for user: {}", user.getId());

        session.setAttribute("userId", user.getId());

        // Populate the security context so Spring Security recognizes this session as authenticated.
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(user.getId(), null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutUser(HttpSession session) {
        Object userId = session.getAttribute("userId");
        session.invalidate();
        SecurityContextHolder.clearContext();

        log.info("Logged out user: {}", userId);
    }

    // Returns the raw CSRF token so clients don't have to read the XSRF-TOKEN cookie.
    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpServletRequest request) {
        DeferredCsrfToken deferred = (DeferredCsrfToken) request.getAttribute(DeferredCsrfToken.class.getName());
        return Map.of("token", deferred.get().getToken());
    }

    // Returns the user tied to the current session, or 401 when no one is logged in.
    @GetMapping("/me")
    public UserResponse getUserProfile(@SessionAttribute(name = "userId", required = false) UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return this.service.getUser(userId)
            .map(UserResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    
}
