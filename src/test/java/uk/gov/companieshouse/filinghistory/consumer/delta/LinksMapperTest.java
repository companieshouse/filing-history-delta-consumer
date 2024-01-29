package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;

class LinksMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "MzA1Njc0Mjg0N3NqYXNqamQ";

    private final LinksMapper linksMapper = new LinksMapper();

    @Test
    void shouldMapSelfLinkToFilingHistoryItemDataLinksObject() {
        // given
        final FilingHistoryItemDataLinks expected = new FilingHistoryItemDataLinks()
                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID));

        // when
        final FilingHistoryItemDataLinks actual = linksMapper.map(COMPANY_NUMBER, TRANSACTION_ID);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfArgumentsAreNullOrEmpty() {
        // given

        // when
        Executable executable = () -> linksMapper.map("", null);

        // then
        assertThrows(IllegalArgumentException.class, executable);
    }
}
