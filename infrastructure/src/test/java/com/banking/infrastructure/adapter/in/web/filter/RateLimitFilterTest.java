package com.banking.infrastructure.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@DisplayName("RateLimitFilter")
@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    private static final int REQUESTS_PER_MINUTE = 5;
    private static final String CLIENT_IP = "192.168.1.100";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(REQUESTS_PER_MINUTE);
    }

    @Nested
    @DisplayName("given request within rate limit")
    class GivenRequestWithinRateLimit {

        @Test
        @DisplayName("when filter called then passes to chain")
        void given_request_within_limit_when_filter_then_passes_to_chain() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("when multiple requests within limit then all pass")
        void given_multiple_requests_within_limit_when_filter_then_all_pass() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            // when
            for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
                rateLimitFilter.doFilter(request, response, filterChain);
            }

            // then
            then(filterChain).should(times(REQUESTS_PER_MINUTE)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("given request exceeding rate limit")
    class GivenRequestExceedingRateLimit {

        @Test
        @DisplayName("when limit exceeded then returns 429")
        void given_limit_exceeded_when_filter_then_returns_429() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            var stringWriter = new StringWriter();
            var printWriter = new PrintWriter(stringWriter);
            given(response.getWriter()).willReturn(printWriter);

            for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
                rateLimitFilter.doFilter(request, response, filterChain);
            }

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(response).should().setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }

        @Test
        @DisplayName("when limit exceeded then sets content type to json")
        void given_limit_exceeded_when_filter_then_sets_content_type() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            var stringWriter = new StringWriter();
            var printWriter = new PrintWriter(stringWriter);
            given(response.getWriter()).willReturn(printWriter);

            for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
                rateLimitFilter.doFilter(request, response, filterChain);
            }

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(response).should().setContentType("application/json");
        }

        @Test
        @DisplayName("when limit exceeded then writes error response")
        void given_limit_exceeded_when_filter_then_writes_error() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            var stringWriter = new StringWriter();
            var printWriter = new PrintWriter(stringWriter);
            given(response.getWriter()).willReturn(printWriter);

            for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
                rateLimitFilter.doFilter(request, response, filterChain);
            }

            // when
            rateLimitFilter.doFilter(request, response, filterChain);
            printWriter.flush();

            // then
            var responseBody = stringWriter.toString();
            assertThat(responseBody).contains("Too Many Requests");
            assertThat(responseBody).contains("429");
        }

        @Test
        @DisplayName("when limit exceeded then does not pass to chain")
        void given_limit_exceeded_when_filter_then_does_not_pass_to_chain() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn(null);
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            var stringWriter = new StringWriter();
            var printWriter = new PrintWriter(stringWriter);
            given(response.getWriter()).willReturn(printWriter);

            for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
                rateLimitFilter.doFilter(request, response, filterChain);
            }

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(filterChain).should(times(REQUESTS_PER_MINUTE)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("given X-Forwarded-For header")
    class GivenXForwardedForHeader {

        @Test
        @DisplayName("when header present then uses first IP from header")
        void given_x_forwarded_for_when_filter_then_uses_header_ip() throws ServletException, IOException {
            // given
            var headerIp = "10.0.0.1";
            given(request.getHeader("X-Forwarded-For")).willReturn(headerIp);

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
            then(request).should(never()).getRemoteAddr();
        }

        @Test
        @DisplayName("when header has multiple IPs then uses first")
        void given_multiple_ips_in_header_when_filter_then_uses_first() throws ServletException, IOException {
            // given
            var multipleIps = "10.0.0.1, 10.0.0.2, 10.0.0.3";
            given(request.getHeader("X-Forwarded-For")).willReturn(multipleIps);

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("when header is empty then falls back to remote addr")
        void given_empty_header_when_filter_then_uses_remote_addr() throws ServletException, IOException {
            // given
            given(request.getHeader("X-Forwarded-For")).willReturn("");
            given(request.getRemoteAddr()).willReturn(CLIENT_IP);

            // when
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(filterChain).should().doFilter(request, response);
            then(request).should().getRemoteAddr();
        }
    }

    @Nested
    @DisplayName("given different clients")
    class GivenDifferentClients {

        @Test
        @DisplayName("when different IPs then separate rate limits")
        void given_different_ips_when_filter_then_separate_limits() throws ServletException, IOException {
            // given
            var clientIp1 = "192.168.1.1";
            var clientIp2 = "192.168.1.2";

            given(request.getHeader("X-Forwarded-For")).willReturn(null);

            given(request.getRemoteAddr()).willReturn(clientIp1);
            for (int i = 0; i < REQUESTS_PER_MINUTE; i++) {
                rateLimitFilter.doFilter(request, response, filterChain);
            }

            // when
            given(request.getRemoteAddr()).willReturn(clientIp2);
            rateLimitFilter.doFilter(request, response, filterChain);

            // then
            then(filterChain).should(times(REQUESTS_PER_MINUTE + 1)).doFilter(request, response);
        }
    }
}
