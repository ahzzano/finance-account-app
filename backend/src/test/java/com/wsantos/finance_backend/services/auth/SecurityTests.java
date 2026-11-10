package com.wsantos.finance_backend.services.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.wsantos.finance_backend.IntegrationTests;

import jakarta.servlet.http.Cookie;

public class SecurityTests extends IntegrationTests {
    private static final String PASSWORD = "roman-carax-my-beloved";

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

    private Cookie registerAndLogin(String email) {
        assertThat(register(email, PASSWORD)).hasStatus(HttpStatus.CREATED);
        MvcTestResult loginResult = login(email, PASSWORD);
        assertThat(loginResult).hasStatus(HttpStatus.OK);
        return loginResult.getResponse().getCookie("SESSION");
    }
}
