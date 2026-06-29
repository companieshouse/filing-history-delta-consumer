package uk.gov.companieshouse.filinghistory.consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.Environment;

import java.util.stream.Stream;

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

    @ParameterizedTest(name = "should throw when salt=''{0}'', apiKey=''{1}'', apiUrl=''{2}''")
    @MethodSource("missingVariableCases")
    void shouldThrowExceptionWhenAnyRequiredEnvironmentVariableIsMissing(
            String transactionIdSalt, String apiKey, String apiUrl) {

        when(environment.getProperty("TRANSACTION_ID_SALT", "")).thenReturn(transactionIdSalt);
        when(environment.getProperty("FILING_HISTORY_API_KEY", "")).thenReturn(apiKey);
        when(environment.getProperty("API_LOCAL_URL", "")).thenReturn(apiUrl);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new SecretsStartupValidator(environment));

        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    private static Stream<Arguments> missingVariableCases() {
        return Stream.of(
                Arguments.of("", "some-api-key", "https://example.com"), // Missing TRANSACTION_ID_SALT
                Arguments.of("some-salt", "", "https://example.com"),    // Missing FILING_HISTORY_API_KEY
                Arguments.of("some-salt", "some-api-key", "")            // Missing API_LOCAL_URL
        );
    }
}
