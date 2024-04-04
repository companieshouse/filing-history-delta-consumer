package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@ExtendWith(MockitoExtension.class)
class TransactionKindServiceTest {

    @InjectMocks
    private TransactionKindService kindService;

    @Mock
    private FormTypeService formTypeService;

    @BeforeEach
    void setUp() {
        kindService = new TransactionKindService(formTypeService, "salt");
    }

    @Test
    void shouldSuccessfullyEncodeIdByEntityIdWhenFormTypeServiceReturnsTopLevel() {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria("entityId", "", "TM01", "", "");

        when(formTypeService.isAssociatedFiling(any())).thenReturn(false);

        TransactionKindResult expected = new TransactionKindResult("ZW50aXR5SWRzYWx0", TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "ANNOTATION , annotation",
            "ASSOCIATED-FILING , associated-filing"
    })
    void shouldSuccessfullyEncodeIdByParentEntityIdWhenFormTypeServiceReturnsNonTopLevel(final String returnedFormType,
                                                                                         final String expectedEnumValue) {
        // given
        TransactionKindCriteria criteria = new TransactionKindCriteria("entityId", "parentEntityId", "ANY", "ANY", "");

        when(formTypeService.getFormType(any())).thenReturn(returnedFormType);

        TransactionKindResult expected = new TransactionKindResult("cGFyZW50RW50aXR5SWRzYWx0", TransactionKindEnum.fromValue(expectedEnumValue));

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

        when(formTypeService.getFormType(any())).thenReturn("TOP-LEVEL");

        TransactionKindResult expected = new TransactionKindResult(entityId, TransactionKindEnum.TOP_LEVEL);

        // when
        TransactionKindResult actual = kindService.encodeIdByTransactionKind(criteria);

        // then
        assertEquals(expected, actual);
    }
}