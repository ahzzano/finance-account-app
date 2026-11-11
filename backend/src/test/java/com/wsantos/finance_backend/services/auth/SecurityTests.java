package com.wsantos.finance_backend.services.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.wsantos.finance_backend.IntegrationTests;

import jakarta.servlet.http.Cookie;

public class SecurityTests extends IntegrationTests {
    private static final String PASSWORD = "roman-carax-my-beloved";

    @Autowired
    private Environment environment;

    @Test
    void getAuthMeWithoutSessionIsUnauthorized() {
        assertThat(mvc.get().uri("/auth/me")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    // No controller maps this route, so only the filter chain can answer 401.
    @Test
    void getUnknownApiRouteWithoutSessionIsUnauthorizedWithoutRedirect() {
        MvcTestResult result = mvc.get().uri("/api/does-not-exist").exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result.getResponse().getHeader("Location")).isNull();
    }

    @Test
    void getCurrenciesAnonymousSucceedsAndSetsCsrfCookie() {
        MvcTestResult result = mvc.get().uri("/api/currencies").exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result.getResponse().getCookie("XSRF-TOKEN")).isNotNull();
    }

    @Test
    void loginWithoutCsrfIsForbiddenButSucceedsWithIt() {
        String email = "lucy+nocsrf@gmail.com";
        assertThat(register(email, PASSWORD)).hasStatus(HttpStatus.CREATED);

        MvcTestResult withoutToken = mvc.post().uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(email, PASSWORD))
            .exchange();

        assertThat(withoutToken).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(login(email, PASSWORD)).hasStatus(HttpStatus.OK);
    }

    @Test
    void loginWithMismatchedCsrfHeaderIsForbidden() {
        String email = "lucy+mismatch@gmail.com";
        assertThat(register(email, PASSWORD)).hasStatus(HttpStatus.CREATED);

        MvcTestResult result = mvc.post().uri("/auth/login")
            .cookie(new Cookie("XSRF-TOKEN", "cookie-token"))
            .header("X-XSRF-TOKEN", "different-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(email, PASSWORD))
            .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void loginWithRealDoubleSubmitCsrfSucceeds() {
        String email = "lucy+doublesubmit@gmail.com";
        assertThat(register(email, PASSWORD)).hasStatus(HttpStatus.CREATED);

        Cookie xsrfCookie = mvc.get().uri("/api/currencies").exchange().getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrfCookie).isNotNull();

        MvcTestResult result = mvc.post().uri("/auth/login")
            .cookie(xsrfCookie)
            .header("X-XSRF-TOKEN", xsrfCookie.getValue())
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(email, PASSWORD))
            .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
    }

    @Test
    void getCsrfAnonymousReturnsRawTokenUsableAsHeader() {
        String email = "lucy+csrfendpoint@gmail.com";
        assertThat(register(email, PASSWORD)).hasStatus(HttpStatus.CREATED);

        MvcTestResult csrfResult = mvc.get().uri("/auth/csrf").exchange();
        assertThat(csrfResult).hasStatus(HttpStatus.OK);

        Cookie xsrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrfCookie).isNotNull();

        String token = assertThat(csrfResult).bodyJson().extractingPath("$.token").asString().actual();
        assertThat(token).isEqualTo(xsrfCookie.getValue());

        MvcTestResult result = mvc.post().uri("/auth/login")
            .cookie(xsrfCookie)
            .header("X-XSRF-TOKEN", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(email, PASSWORD))
            .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
    }

    @Test
    void getAuthMeWithSessionReturnsUser() {
        String email = "lucy+me@gmail.com";
        Cookie sessionCookie = registerAndLogin(email);

        assertThat(mvc.get().uri("/auth/me").cookie(sessionCookie))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .extractingPath("$.email")
            .isEqualTo(email);
    }

    @Test
    void logoutWithSessionButWithoutCsrfIsForbiddenAndKeepsSession() {
        Cookie sessionCookie = registerAndLogin("lucy+logoutnocsrf@gmail.com");

        assertThat(mvc.post().uri("/auth/logout").cookie(sessionCookie)).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(mvc.get().uri("/auth/me").cookie(sessionCookie)).hasStatus(HttpStatus.OK);
    }

    @Test
    void authenticatedDeleteRequiresCsrf() {
        Cookie sessionCookie = registerAndLogin("lucy+delete@gmail.com");

        assertThat(mvc.delete().uri("/api/does-not-exist").cookie(sessionCookie)).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(mvc.delete().uri("/api/does-not-exist").cookie(sessionCookie).with(xsrf())).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void loginRotatesExistingSessionId() {
        String emailA = "lucy+rotatea@gmail.com";
        String emailB = "lucy+rotateb@gmail.com";
        Cookie s1 = registerAndLogin(emailA);
        assertThat(register(emailB, PASSWORD)).hasStatus(HttpStatus.CREATED);

        MvcTestResult result = mvc.post().uri("/auth/login")
            .cookie(s1)
            .with(xsrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(emailB, PASSWORD))
            .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);

        Cookie rotated = result.getResponse().getCookie("SESSION");
        assertThat(rotated).isNotNull();
        assertThat(rotated.getValue()).isNotEqualTo(s1.getValue());

        assertThat(mvc.get().uri("/auth/me").cookie(rotated))
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .extractingPath("$.email")
            .isEqualTo(emailB);

        assertThat(mvc.get().uri("/auth/me").cookie(s1)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void sessionTimeoutDefaultsToSevenDays() {
        Duration timeout = environment.getProperty("spring.session.timeout", Duration.class);
        assertThat(timeout).isEqualTo(Duration.ofDays(7));
    }

    private Cookie registerAndLogin(String email) {
        assertThat(register(email, PASSWORD)).hasStatus(HttpStatus.CREATED);
        MvcTestResult loginResult = login(email, PASSWORD);
        assertThat(loginResult).hasStatus(HttpStatus.OK);
        return loginResult.getResponse().getCookie("SESSION");
    }
}
