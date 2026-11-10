package com.wsantos.finance_backend.services.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.wsantos.finance_backend.IntegrationTests;
import com.wsantos.finance_backend.services.auth.domain.AppUser;

import jakarta.servlet.http.Cookie;

public class AuthControllerTests extends IntegrationTests{
    @Autowired 
    AuthRepository repository;

    @Autowired 
    SessionRepository<? extends Session> sessionRepository;

    @Test
    void successfulRegistration() {
        assertThat(register("talia.yang@gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);

        AppUser user = repository.findAll().stream().filter(u -> u.getEmail().equals("talia.yang@gmail.com")).findFirst().orElseThrow();

        assertThat(user.getEmail()).isEqualTo("talia.yang@gmail.com");
        assertThat(user.getPasswordHash()).isNotEqualTo("roman-carax-my-beloved");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCurrency().getCode()).isEqualTo("USD");

    }

    @Test
    void successfulLogin() {
        assertThat(register("talia.yang+login@gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);
        MvcTestResult result = login("talia.yang+login@gmail.com", "roman-carax-my-beloved");
        assertThat(result).hasStatus(HttpStatus.OK);

        Session session = sessionRepository.findById(sessionId(result.getResponse().getCookie("SESSION")));
        AppUser user = repository.findByEmailIgnoreCase("talia.yang+login@gmail.com").orElseThrow();

        assertThat(session).isNotNull();
        assertThat((Object) session.getAttribute("userId")).isEqualTo(user.getId());
    }

    @Test
    void successfulLogout() {
        register("talia.yang+logout@gmail.com", "roman-carax-my-beloved");
        MvcTestResult loginResult = login("talia.yang+logout@gmail.com", "roman-carax-my-beloved");
        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        String sessionId = sessionId(sessionCookie);
        assertThat(sessionRepository.findById(sessionId)).isNotNull();

        MvcTestResult result = mvc.post().uri("/auth/logout").with(xsrf()).cookie(sessionCookie).exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(sessionRepository.findById(sessionId)).isNull();
    }

    // Spring Session stores the session ID Base64-encoded in the SESSION cookie.
    private String sessionId(Cookie sessionCookie) {
        return new String(Base64.getDecoder().decode(sessionCookie.getValue()));
    }
    
}
