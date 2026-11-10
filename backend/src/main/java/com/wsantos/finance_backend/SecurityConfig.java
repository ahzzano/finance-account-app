package com.wsantos.finance_backend;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Configures session-based HTTP security with SPA CSRF and bare 401/403 responses. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        AuthenticationEntryPoint unauthorizedEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);

        http
            .securityContext(context -> context.securityContextRepository(securityContextRepository))
            .csrf(csrf -> csrf.spa())
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/currencies").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(unauthorizedEntryPoint)
                .accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(HttpStatus.FORBIDDEN.value())))
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .logout(logout -> logout.disable())
            .requestCache(cache -> cache.disable());

        return http.build();
    }

    /** Loads the deferred CSRF token on every request so the XSRF-TOKEN cookie is always written. */
    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfToken.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
