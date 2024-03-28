package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

public class FormTypeServiceTest {

    private static final String ANNOTATION_FORM_TYPE = "ANNOTATION";
    private static final String ASSOCIATED_FILING_FORM_TYPE = "ASSOCIATED-FILING";
    private static final String TOP_LEVEL_FORM_TYPE = "TOP-LEVEL";

    private final FormTypeService formTypeService = new FormTypeService();

    @Test
    void shouldReturnAnnotationTypeWhenPassedAnnotation() {
        // given
        TransactionKindCriteria transactionKindCriteria = new TransactionKindCriteria(
                "12345",
                "67891",
                "",
                ANNOTATION_FORM_TYPE,
                null
        );

        // when
        final String actual = formTypeService.getFormType(transactionKindCriteria);

        // then
        assertEquals(ANNOTATION_FORM_TYPE, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "random_string , ''",
            "'' , random_string"
    })
    void shouldReturnAssociatedFilingTypeWhenPassedRandomStringAndHasParentEntityId(final String formType, final String parentFormType) {
        // given
        TransactionKindCriteria transactionKindCriteria = new TransactionKindCriteria(
                "12345",
                "67891",
                formType,
                parentFormType,
                null
        );

        // when
        final String actual = formTypeService.getFormType(transactionKindCriteria);

        // then
        assertEquals(ASSOCIATED_FILING_FORM_TYPE, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "random_string , ''",
            "'' , random_string"
    })
    void shouldReturnTopLevelTypeWhenPassedRandomStringAndDoesNotHaveParentEntityId(final String formType, final String parentFormType) {
        // given
        TransactionKindCriteria transactionKindCriteria = new TransactionKindCriteria(
                "12345",
                "",
                formType,
                parentFormType,
                null
        );

        // when
        final String actual = formTypeService.getFormType(transactionKindCriteria);

        // then
        assertEquals(TOP_LEVEL_FORM_TYPE, actual);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/associated_filings_blacklist.csv")
    void shouldReturnTopLevelTypeWhenPassedBlacklistEntriesForFormType(final String formType) {
        // given
        TransactionKindCriteria transactionKindCriteria = new TransactionKindCriteria(
                "12345",
                "67891",
                formType,
                "",
                null
        );

        // when
        final String actual = formTypeService.getFormType(transactionKindCriteria);

        // then
        assertEquals(TOP_LEVEL_FORM_TYPE, actual);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/associated_filings_blacklist.csv")
    void shouldReturnTopLevelTypeWhenPassedBlacklistEntriesForParentFormType(final String formType) {
        // given
        TransactionKindCriteria transactionKindCriteria = new TransactionKindCriteria(
                "12345",
                "67891",
                "",
                formType,
                null
        );

        // when
        final String actual = formTypeService.getFormType(transactionKindCriteria);

        // then
        assertEquals(TOP_LEVEL_FORM_TYPE, actual);
    }
}
