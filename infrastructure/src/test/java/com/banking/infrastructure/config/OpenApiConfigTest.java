package com.banking.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenApiConfig")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        ReflectionTestUtils.setField(openApiConfig, "serverPort", 8081);
    }

    @Nested
    @DisplayName("given OpenAPI bean")
    class GivenOpenApiBean {

        @Test
        @DisplayName("when customOpenAPI called then returns configured OpenAPI")
        void given_config_when_custom_open_api_then_returns_configured() {
            // when
            var openAPI = openApiConfig.customOpenAPI();

            // then
            assertThat(openAPI).isNotNull();
        }

        @Test
        @DisplayName("when customOpenAPI called then info is configured")
        void given_config_when_custom_open_api_then_info_configured() {
            // when
            var openAPI = openApiConfig.customOpenAPI();

            // then
            assertThat(openAPI.getInfo()).isNotNull();
            assertThat(openAPI.getInfo().getTitle()).isEqualTo("Bank Transaction Aggregator API");
            assertThat(openAPI.getInfo().getDescription()).contains("REST API");
            assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        }

        @Test
        @DisplayName("when customOpenAPI called then contact is configured")
        void given_config_when_custom_open_api_then_contact_configured() {
            // when
            var openAPI = openApiConfig.customOpenAPI();

            // then
            assertThat(openAPI.getInfo().getContact()).isNotNull();
            assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("API Support");
            assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("support@example.com");
        }

        @Test
        @DisplayName("when customOpenAPI called then servers are configured")
        void given_config_when_custom_open_api_then_servers_configured() {
            // when
            var openAPI = openApiConfig.customOpenAPI();

            // then
            assertThat(openAPI.getServers()).isNotNull();
            assertThat(openAPI.getServers()).hasSize(1);
        }

        @Test
        @DisplayName("when customOpenAPI called then server url contains port")
        void given_config_when_custom_open_api_then_server_url_correct() {
            // when
            var openAPI = openApiConfig.customOpenAPI();

            // then
            var server = openAPI.getServers().get(0);
            assertThat(server.getUrl()).contains("localhost");
            assertThat(server.getUrl()).contains("8081");
            assertThat(server.getDescription()).isEqualTo("Local development server");
        }
    }

    @Nested
    @DisplayName("given different port")
    class GivenDifferentPort {

        @Test
        @DisplayName("when port is 9090 then server url contains 9090")
        void given_port_9090_when_custom_open_api_then_url_contains_9090() {
            // given
            ReflectionTestUtils.setField(openApiConfig, "serverPort", 9090);

            // when
            var openAPI = openApiConfig.customOpenAPI();

            // then
            var server = openAPI.getServers().get(0);
            assertThat(server.getUrl()).contains("9090");
        }
    }
}
