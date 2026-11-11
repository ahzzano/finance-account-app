package com.wsantos.finance_backend.services.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.wsantos.finance_backend.TestcontainersConfiguration;

// MockMvc counts as a WAR deployment and ignores server.servlet.session.cookie.*, so this needs a real embedded server.
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "DB_SCHEMA=public")
@Import(TestcontainersConfiguration.class)
public class SessionCookieTests {
    private static final String PASSWORD = "roman-carax-my-beloved";

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void loginSetsHardenedSessionCookie() throws Exception {
        String credentials = """
            {"email": "lucy+cookieflags@gmail.com", "password": "%s" }
            """.formatted(PASSWORD);
        assertThat(post("/auth/register", credentials).statusCode()).isEqualTo(201);

        HttpResponse<String> login = post("/auth/login", credentials);
        assertThat(login.statusCode()).isEqualTo(200);

        String sessionCookie = login.headers().allValues("Set-Cookie").stream()
            .filter(header -> header.startsWith("SESSION="))
            .findFirst()
            .orElseThrow();
        assertThat(sessionCookie).contains("HttpOnly").contains("Secure").contains("SameSite=Lax");
    }

    // Sends a JSON POST with a matching double-submit CSRF cookie and header.
    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .header("Cookie", "XSRF-TOKEN=test-csrf-token")
            .header("X-XSRF-TOKEN", "test-csrf-token")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
