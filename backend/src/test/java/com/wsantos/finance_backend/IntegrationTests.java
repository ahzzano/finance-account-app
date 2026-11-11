package com.wsantos.finance_backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.servlet.http.Cookie;

@SpringBootTest(properties = "DB_SCHEMA=public")
@AutoConfigureMockMvc 
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTests {
    @Autowired 
    protected MockMvcTester mvc;

    // Real double-submit CSRF (cookie + matching header); Spring's csrf() helper mutates the shared filter chain.
    protected static RequestPostProcessor xsrf() {
        return request -> {
            List<Cookie> cookies = new ArrayList<>();
            if (request.getCookies() != null) {
                List.of(request.getCookies()).stream()
                    .filter(cookie -> !cookie.getName().equals("XSRF-TOKEN"))
                    .forEach(cookies::add);
            }
            cookies.add(new Cookie("XSRF-TOKEN", "test-csrf-token"));
            request.setCookies(cookies.toArray(Cookie[]::new));
            request.addHeader("X-XSRF-TOKEN", "test-csrf-token");
            return request;
        };
    }

    protected MvcTestResult register(String email, String password) {
        return mvc.post().uri("/auth/register")
            .with(xsrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(email, password))
            .exchange();
    }

    protected MvcTestResult login(String email, String password) {
        return mvc.post().uri("/auth/login")
            .with(xsrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(credentialsJson(email, password))
            .exchange();
    }

    protected static String credentialsJson(String email, String password) {
        return """
            {"email": "%s", "password": "%s" }
            """.formatted(email, password);
    }

}
