package com.wsantos.finance_backend;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ProblemDetailTests.FailingController.class)
@WithMockUser
class ProblemDetailTests extends IntegrationTests {

    @RestController
    static class FailingController {
        @GetMapping("/test/fail")
        String fail() {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void unknownRouteReturnsProblemDetail() {
        assertThat(mvc.get().uri("/api/does-not-exist"))
                .hasStatus(404)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo(404);
    }

    @Test
    void unexpectedExceptionReturnsProblemDetailWithoutInternals() {
        assertThat(mvc.get().uri("/test/fail"))
                .hasStatus(500)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("An unexpected error occurred.");
    }
}
