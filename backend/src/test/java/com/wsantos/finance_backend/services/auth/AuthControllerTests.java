package com.wsantos.finance_backend.services.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    void successfulLogout() throws Exception {
        register("talia.yang+logout@gmail.com", "roman-carax-my-beloved");
        MvcTestResult loginResult = login("talia.yang+logout@gmail.com", "roman-carax-my-beloved");
        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");
        String sessionId = sessionId(sessionCookie);
        assertThat(sessionRepository.findById(sessionId)).isNotNull();

        MvcTestResult result = mvc.post().uri("/auth/logout").with(xsrf()).cookie(sessionCookie).exchange();

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(result.getResponse().getContentAsString()).isEmpty();
        assertThat(sessionRepository.findById(sessionId)).isNull();
    }

    @Test
    void registerResponseExposesOnlyPublicFields() {
        String email = "talia.yang+publicfields@gmail.com";
        MvcTestResult result = register(email, "roman-carax-my-beloved");

        assertThat(result)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .hasPath("$.id")
            .extractingPath("$.email")
            .isEqualTo(email);

        assertThat(result)
            .bodyJson()
            .extractingPath("$.currencyCode")
            .isEqualTo("USD");

        assertThat(result)
            .bodyJson()
            .doesNotHavePath("$.passwordHash")
            .doesNotHavePath("$.currency")
            .doesNotHavePath("$.emailVerifiedAt")
            .doesNotHavePath("$.createdAt")
            .doesNotHavePath("$.updatedAt");
    }

    @Test
    void loginResponseOmitsPasswordHash() {
        String email = "talia.yang+loginpublic@gmail.com";
        assertThat(register(email, "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);

        MvcTestResult result = login(email, "roman-carax-my-beloved");
        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .extractingPath("$.email")
            .isEqualTo(email);

        assertThat(result)
            .bodyJson()
            .doesNotHavePath("$.passwordHash");
    }

    @Test
    void meResponseOmitsPasswordHash() {
        String email = "talia.yang+mepublic@gmail.com";
        assertThat(register(email, "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);
        MvcTestResult loginResult = login(email, "roman-carax-my-beloved");
        assertThat(loginResult).hasStatus(HttpStatus.OK);
        Cookie sessionCookie = loginResult.getResponse().getCookie("SESSION");

        MvcTestResult result = mvc.get().uri("/auth/me").cookie(sessionCookie).exchange();
        assertThat(result)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .extractingPath("$.email")
            .isEqualTo(email);

        assertThat(result)
            .bodyJson()
            .doesNotHavePath("$.passwordHash");
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        assertThat(register("talia.yang+wrongpass@gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);

        assertThat(login("talia.yang+wrongpass@gmail.com", "not-the-right-password"))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .extractingPath("$.detail")
            .isEqualTo("Invalid email or password.");
    }

    @Test
    void loginWithUnknownEmailReturns401() {
        assertThat(login("talia.yang+unknown@gmail.com", "roman-carax-my-beloved"))
            .hasStatus(HttpStatus.UNAUTHORIZED)
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .extractingPath("$.detail")
            .isEqualTo("Invalid email or password.");
    }

    @Test
    void loginFailuresAreIndistinguishable() {
        assertThat(register("talia.yang+indistinguishable@gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);

        MvcTestResult wrongPassword = login("talia.yang+indistinguishable@gmail.com", "not-the-right-password");
        MvcTestResult unknownEmail = login("talia.yang+no-such-user@gmail.com", "roman-carax-my-beloved");

        assertThat(wrongPassword.getResponse().getStatus()).isEqualTo(unknownEmail.getResponse().getStatus());
        assertThat(wrongPassword).bodyJson().extractingPath("$.detail").isEqualTo("Invalid email or password.");
        assertThat(unknownEmail).bodyJson().extractingPath("$.detail").isEqualTo("Invalid email or password.");
    }

    @Test
    void duplicateRegistrationReturns409() {
        assertThat(register("talia.yang+dupe@gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);

        assertThat(register("talia.yang+dupe@gmail.com", "roman-carax-my-beloved"))
            .hasStatus(HttpStatus.CONFLICT)
            .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyJson()
            .extractingPath("$.detail")
            .isEqualTo("Email is already registered.");
    }

    @Test
    void duplicateRegistrationIsCaseInsensitive() {
        assertThat(register("Talia.Yang+Dup@Gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CREATED);

        assertThat(register("talia.yang+dup@gmail.com", "roman-carax-my-beloved")).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void duplicateRegistrationKeepsOneUser() {
        register("talia.yang+dupkeep@gmail.com", "roman-carax-my-beloved");
        register("talia.yang+dupkeep@gmail.com", "roman-carax-my-beloved");

        long matches = repository.findAll().stream()
            .filter(u -> u.getEmail().equalsIgnoreCase("talia.yang+dupkeep@gmail.com"))
            .count();

        assertThat(matches).isEqualTo(1);
    }

    @Test
    void registrationWithPasswordOver72BytesReturns400() {
        String email = "talia.yang+longpass@gmail.com";
        String password = "é".repeat(37);

        assertThat(register(email, password)).hasStatus(HttpStatus.BAD_REQUEST);

        boolean saved = repository.findAll().stream()
            .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        assertThat(saved).isFalse();
    }

    @Test
    void registrationWithPasswordOf72BytesSucceeds() {
        String email = "talia.yang+maxpass@gmail.com";
        String password = "a".repeat(72);

        assertThat(register(email, password)).hasStatus(HttpStatus.CREATED);
    }

    // Spring Session stores the session ID Base64-encoded in the SESSION cookie.
    private String sessionId(Cookie sessionCookie) {
        return new String(Base64.getDecoder().decode(sessionCookie.getValue()));
    }
    
}
