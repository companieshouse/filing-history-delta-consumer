package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class FormTypeServiceTest {

    private final FormTypeService formTypeService = new FormTypeService(List.of("1(Scot)", "MR09"));

    @ParameterizedTest
    @MethodSource("isAssociatedFilingBlockListedScenarios")
    void shouldReturnAssociatedFilingTypeWhenPassedRandomStringAndHasParentEntityId(
            TransactionKindCriteria kindCriteria, final boolean expected) {
        // given

        // when
        final boolean actual = formTypeService.isAssociatedFilingBlockListed(kindCriteria);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "(W)ELRES", "ELRES", "ELRES S252", "ELRES S366A", "ELRES S369(4)", "ELRES S80A", "ERES01", "LRES(NI)",
            "LRESC(NI)", "LRESEX", "LRESM(NI)", "LRESSP", "ORES01", "RES 17", "RES(ECS)", "RES(NI)", "RES(U)", "RES01",
            "RES02(U)", "RES15", "RESMISC", "SRES01", "WRES01"
    })
    void shouldReturnTrueWhenFormTypeMatchesResolutionRegex(String formType) {
        // form types taken from transform_rules.yml with similar/duplicate types removed

        // given

        // when
        final boolean actual = formTypeService.isResolutionType(formType);

        // then
        assertTrue(actual);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ANNOTATION", "AP01", "MR01", "''", "null"
    }, nullValues = {"null"})
    void shouldReturnFalseWhenFormTypeDoesNotMatchResolutionRegex(String formType) {
        // given

        // when
        final boolean actual = formTypeService.isResolutionType(formType);

        // then
        assertFalse(actual);
    }

    private static Stream<Arguments> isAssociatedFilingBlockListedScenarios() {
        return Stream.of(
                Arguments.of(new TransactionKindCriteria(
                        "12345",
                        "67891",
                        "any string not on block list",
                        "",
                        null), false),
                Arguments.of(new TransactionKindCriteria(
                        "12345",
                        "67891",
                        "",
                        "any string not on block list",
                        null), false),
                Arguments.of(new TransactionKindCriteria(
                        "12345",
                        "67891",
                        "1(Scot)",
                        "",
                        null), true),
                Arguments.of(new TransactionKindCriteria(
                        "12345",
                        "67891",
                        "",
                        "1(Scot)",
                        null), true)
        );
    }
}
