package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

class TransactionKindServiceTest {

    private static final String SALT = "salt";

    private TransactionKindService kindService;

    @BeforeEach
    void setUp() {
        kindService = new TransactionKindService(new FormTypeService(Collections.emptyList()), SALT);
    }

    @Test
    void shouldSuccessfullyEncodeIdByTransactionKind() {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria("entityId", "", "TM01", "", "");

        TransactionKindResult expected = new TransactionKindResult("ZW50aXR5SWRzYWx0", TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null",
            "''"
    },
    nullValues = {"null"})
    void shouldReturnUnchangedIdIfNullOrEmpty(final String entityId) {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria(entityId, "", "TM01", "", "");

        TransactionKindResult expected = new TransactionKindResult(entityId, TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }
}