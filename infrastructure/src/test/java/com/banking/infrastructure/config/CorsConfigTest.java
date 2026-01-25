package com.banking.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorsConfig")
class CorsConfigTest {

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
    }

    @Nested
    @DisplayName("given CorsConfig bean")
    class GivenCorsConfigBean {

        @Test
        @DisplayName("when corsFilter called then returns CorsFilter instance")
        void given_config_when_cors_filter_then_returns_filter() {
            // when
            var filter = corsConfig.corsFilter();

            // then
            assertThat(filter).isNotNull();
            assertThat(filter).isInstanceOf(CorsFilter.class);
        }

        @Test
        @DisplayName("when corsFilter called multiple times then returns new instances")
        void given_config_when_cors_filter_multiple_then_new_instances() {
            // when
            var filter1 = corsConfig.corsFilter();
            var filter2 = corsConfig.corsFilter();

            // then
            assertThat(filter1).isNotNull();
            assertThat(filter2).isNotNull();
            assertThat(filter1).isNotSameAs(filter2);
        }
    }
}
