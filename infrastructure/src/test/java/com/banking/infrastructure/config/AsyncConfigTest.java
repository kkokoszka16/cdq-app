package com.banking.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AsyncConfig.
 */
@DisplayName("AsyncConfig")
class AsyncConfigTest {

    @Nested
    @DisplayName("given importTaskExecutor bean")
    class GivenImportTaskExecutorBean {

        @Test
        @DisplayName("when created then returns ThreadPoolTaskExecutor")
        void given_config_when_create_executor_then_returns_thread_pool_executor() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = config.importTaskExecutor();

            // then
            assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        }

        @Test
        @DisplayName("when created then has correct core pool size")
        void given_config_when_create_executor_then_has_correct_core_pool_size() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = (ThreadPoolTaskExecutor) config.importTaskExecutor();

            // then
            assertThat(executor.getCorePoolSize()).isEqualTo(4);
        }

        @Test
        @DisplayName("when created then has correct max pool size")
        void given_config_when_create_executor_then_has_correct_max_pool_size() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = (ThreadPoolTaskExecutor) config.importTaskExecutor();

            // then
            assertThat(executor.getMaxPoolSize()).isEqualTo(8);
        }

        @Test
        @DisplayName("when created then has correct thread name prefix")
        void given_config_when_create_executor_then_has_correct_thread_prefix() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = (ThreadPoolTaskExecutor) config.importTaskExecutor();

            // then
            assertThat(executor.getThreadNamePrefix()).isEqualTo("import-");
        }

        @Test
        @DisplayName("when created then uses CallerRunsPolicy for rejection")
        void given_config_when_create_executor_then_uses_caller_runs_policy() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = (ThreadPoolTaskExecutor) config.importTaskExecutor();

            // then
            assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
        }

        @Test
        @DisplayName("when created then is initialized")
        void given_config_when_create_executor_then_is_initialized() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = (ThreadPoolTaskExecutor) config.importTaskExecutor();

            // then
            assertThat(executor.getThreadPoolExecutor()).isNotNull();
        }

        @Test
        @DisplayName("when created then implements Executor interface")
        void given_config_when_create_executor_then_implements_executor() {
            // given
            var config = new AsyncConfig();

            // when
            var executor = config.importTaskExecutor();

            // then
            assertThat(executor).isInstanceOf(Executor.class);
        }
    }
}
