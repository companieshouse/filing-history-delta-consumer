package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;

class FormTypeConfigTest {

    private final FormTypeConfig formTypeConfig = new FormTypeConfig();

    @Test
    void shouldBuildBlacklist() {
        // given

        // when
        List<String> actual = formTypeConfig.formTypeBlacklist("associated_filings_blacklist.csv");

        // then
        assertEquals(347, actual.size());
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenFileNotFound() {
        // given

        // when
        Executable executable = () -> formTypeConfig.formTypeBlacklist("missing_file.csv");

        // then
        assertThrows(NonRetryableException.class, executable);
    }
}
