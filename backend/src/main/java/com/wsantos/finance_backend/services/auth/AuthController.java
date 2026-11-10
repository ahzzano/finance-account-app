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
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
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
    public AppUser registerUser(@RequestBody @Valid RegistrationForm form) {
        return this.service.registerUser(form);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AppUser loginUser(@RequestBody @Valid LoginForm loginForm, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws LoginException {

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

        return user;
    }

    @PostMapping("/logout")
    public String logoutUser(HttpSession session) {
        String sessionId = session.getId();
        session.invalidate();
        SecurityContextHolder.clearContext();

        log.info("Invalidated session: %s".formatted(sessionId));

        return sessionId;
    }

    // Returns the user tied to the current session, or 401 when no one is logged in.
    @GetMapping("/me")
    public AppUser getUserProfile(@SessionAttribute(name = "userId", required = false) UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return this.service.getUser(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    
}
