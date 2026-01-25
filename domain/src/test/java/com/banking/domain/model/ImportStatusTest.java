package com.banking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImportStatus")
class ImportStatusTest {

    @Nested
    @DisplayName("terminal state checks")
    class TerminalStateChecks {

        @ParameterizedTest(name = "{0} should be terminal: {1}")
        @CsvSource({
                "PENDING, false",
                "PROCESSING, false",
                "COMPLETED, true",
                "FAILED, true"
        })
        @DisplayName("when checking isTerminal then returns correct value")
        void given_status_when_checking_terminal_then_returns_expected(ImportStatus status, boolean expectedTerminal) {
            // when
            var result = status.isTerminal();

            // then
            assertThat(result).isEqualTo(expectedTerminal);
        }
    }

    @Nested
    @DisplayName("state transitions")
    class StateTransitions {

        @Test
        @DisplayName("when pending then can transition to processing")
        void given_pending_when_checking_transition_to_processing_then_allowed() {
            // when
            var result = ImportStatus.PENDING.canTransitionTo(ImportStatus.PROCESSING);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when pending then can transition to failed")
        void given_pending_when_checking_transition_to_failed_then_allowed() {
            // when
            var result = ImportStatus.PENDING.canTransitionTo(ImportStatus.FAILED);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when pending then cannot transition to completed")
        void given_pending_when_checking_transition_to_completed_then_not_allowed() {
            // when
            var result = ImportStatus.PENDING.canTransitionTo(ImportStatus.COMPLETED);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("when pending then cannot transition to pending")
        void given_pending_when_checking_transition_to_pending_then_not_allowed() {
            // when
            var result = ImportStatus.PENDING.canTransitionTo(ImportStatus.PENDING);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("when processing then can transition to completed")
        void given_processing_when_checking_transition_to_completed_then_allowed() {
            // when
            var result = ImportStatus.PROCESSING.canTransitionTo(ImportStatus.COMPLETED);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when processing then can transition to failed")
        void given_processing_when_checking_transition_to_failed_then_allowed() {
            // when
            var result = ImportStatus.PROCESSING.canTransitionTo(ImportStatus.FAILED);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("when processing then cannot transition to pending")
        void given_processing_when_checking_transition_to_pending_then_not_allowed() {
            // when
            var result = ImportStatus.PROCESSING.canTransitionTo(ImportStatus.PENDING);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("when processing then cannot transition to processing")
        void given_processing_when_checking_transition_to_processing_then_not_allowed() {
            // when
            var result = ImportStatus.PROCESSING.canTransitionTo(ImportStatus.PROCESSING);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest(name = "COMPLETED cannot transition to {0}")
        @EnumSource(ImportStatus.class)
        @DisplayName("when completed then cannot transition to any status")
        void given_completed_when_checking_any_transition_then_not_allowed(ImportStatus target) {
            // when
            var result = ImportStatus.COMPLETED.canTransitionTo(target);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest(name = "FAILED cannot transition to {0}")
        @EnumSource(ImportStatus.class)
        @DisplayName("when failed then cannot transition to any status")
        void given_failed_when_checking_any_transition_then_not_allowed(ImportStatus target) {
            // when
            var result = ImportStatus.FAILED.canTransitionTo(target);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("descriptions")
    class Descriptions {

        @ParameterizedTest(name = "{0} has description")
        @EnumSource(ImportStatus.class)
        @DisplayName("when getting description then returns non-empty string")
        void given_any_status_when_getting_description_then_returns_non_empty(ImportStatus status) {
            // when
            var description = status.getDescription();

            // then
            assertThat(description).isNotBlank();
        }

        @Test
        @DisplayName("when getting pending description then contains queued")
        void given_pending_when_getting_description_then_contains_queued() {
            // when
            var description = ImportStatus.PENDING.getDescription();

            // then
            assertThat(description).containsIgnoringCase("queued");
        }

        @Test
        @DisplayName("when getting processing description then contains progress")
        void given_processing_when_getting_description_then_contains_progress() {
            // when
            var description = ImportStatus.PROCESSING.getDescription();

            // then
            assertThat(description).containsIgnoringCase("progress");
        }

        @Test
        @DisplayName("when getting completed description then contains success")
        void given_completed_when_getting_description_then_contains_success() {
            // when
            var description = ImportStatus.COMPLETED.getDescription();

            // then
            assertThat(description).containsIgnoringCase("success");
        }

        @Test
        @DisplayName("when getting failed description then contains failed")
        void given_failed_when_getting_description_then_contains_failed() {
            // when
            var description = ImportStatus.FAILED.getDescription();

            // then
            assertThat(description).containsIgnoringCase("failed");
        }
    }
}
