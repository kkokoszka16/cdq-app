package com.banking.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Nested
    @DisplayName("given SecurityConfig class")
    class GivenSecurityConfigClass {

        @Test
        @DisplayName("when instantiated then not null")
        void given_class_when_instantiated_then_not_null() {
            // then
            assertThat(securityConfig).isNotNull();
        }

        @Test
        @DisplayName("when checked then has Configuration annotation")
        void given_class_when_checked_then_has_configuration_annotation() {
            // then
            var hasAnnotation = SecurityConfig.class
                    .isAnnotationPresent(org.springframework.context.annotation.Configuration.class);
            assertThat(hasAnnotation).isTrue();
        }

        @Test
        @DisplayName("when checked then has EnableWebSecurity annotation")
        void given_class_when_checked_then_has_enable_web_security_annotation() {
            // then
            var hasAnnotation = SecurityConfig.class
                    .isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class);
            assertThat(hasAnnotation).isTrue();
        }

        @Test
        @DisplayName("when securityFilterChain method checked then has Bean annotation")
        void given_method_when_checked_then_has_bean_annotation() throws NoSuchMethodException {
            // given
            var method = SecurityConfig.class.getMethod(
                    "securityFilterChain",
                    org.springframework.security.config.annotation.web.builders.HttpSecurity.class
            );

            // then
            var hasAnnotation = method.isAnnotationPresent(org.springframework.context.annotation.Bean.class);
            assertThat(hasAnnotation).isTrue();
        }

        @Test
        @DisplayName("when securityFilterChain method checked then returns SecurityFilterChain")
        void given_method_when_checked_then_returns_security_filter_chain() throws NoSuchMethodException {
            // given
            var method = SecurityConfig.class.getMethod(
                    "securityFilterChain",
                    org.springframework.security.config.annotation.web.builders.HttpSecurity.class
            );

            // then
            assertThat(method.getReturnType())
                    .isEqualTo(org.springframework.security.web.SecurityFilterChain.class);
        }

        @Test
        @DisplayName("when securityFilterChain method checked then declares Exception")
        void given_method_when_checked_then_declares_exception() throws NoSuchMethodException {
            // given
            var method = SecurityConfig.class.getMethod(
                    "securityFilterChain",
                    org.springframework.security.config.annotation.web.builders.HttpSecurity.class
            );

            // then
            assertThat(method.getExceptionTypes())
                    .contains(Exception.class);
        }
    }
}
