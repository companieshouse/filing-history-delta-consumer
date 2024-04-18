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
    void shouldBuildBlockList() {
        // given

        // when
        List<String> actual = formTypeConfig.formTypeBlockList("associated_filings_block_list.csv");

        // then
        assertEquals(347, actual.size());
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenFileNotFound() {
        // given

        // when
        Executable executable = () -> formTypeConfig.formTypeBlockList("missing_file.csv");

        // then
        assertThrows(NonRetryableException.class, executable);
    }
}
