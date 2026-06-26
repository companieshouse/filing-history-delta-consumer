package uk.gov.companieshouse.filinghistory.consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class SecretsStartupValidatorTest {

    private static final String ERROR_MESSAGE =
            "Refusing to start with default/blank secrets for TRANSACTION_ID_SALT, FILING_HISTORY_API_KEY and/or API_LOCAL_URL";

    @Mock
    private Environment environment;

    @Test
    void shouldNotThrowExceptionWhenAllEnvironmentVariablesArePresent() {
        // given
        when(environment.getProperty("TRANSACTION_ID_SALT", "")).thenReturn("some-salt");
        when(environment.getProperty("FILING_HISTORY_API_KEY", "")).thenReturn("some-api-key");
        when(environment.getProperty("API_LOCAL_URL", "")).thenReturn("https://example.com");

        // when
        Executable executable = () -> new SecretsStartupValidator(environment);

        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void shouldThrowExceptionWhenTransactionIdSaltIsMissing() {
        // given
        when(environment.getProperty("TRANSACTION_ID_SALT", "")).thenReturn("");
        when(environment.getProperty("FILING_HISTORY_API_KEY", "")).thenReturn("some-api-key");
        when(environment.getProperty("API_LOCAL_URL", "")).thenReturn("https://example.com");

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new SecretsStartupValidator(environment));

        // then
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenApiKeyIsMissing() {
        // given
        when(environment.getProperty("TRANSACTION_ID_SALT", "")).thenReturn("some-salt");
        when(environment.getProperty("FILING_HISTORY_API_KEY", "")).thenReturn("");
        when(environment.getProperty("API_LOCAL_URL", "")).thenReturn("https://example.com");

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new SecretsStartupValidator(environment));

        // then
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenApiUrlIsMissing() {
        // given
        when(environment.getProperty("TRANSACTION_ID_SALT", "")).thenReturn("some-salt");
        when(environment.getProperty("FILING_HISTORY_API_KEY", "")).thenReturn("some-api-key");
        when(environment.getProperty("API_LOCAL_URL", "")).thenReturn("");

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new SecretsStartupValidator(environment));

        // then
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }
}
