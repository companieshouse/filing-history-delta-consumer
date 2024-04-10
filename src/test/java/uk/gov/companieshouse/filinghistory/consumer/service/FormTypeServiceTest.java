package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FormTypeServiceTest {

    private final FormTypeService formTypeService = new FormTypeService(List.of("1(Scot)", "MR09"));

    @ParameterizedTest
    @MethodSource("isAssociatedFilingBlacklistedScenarios")
    void shouldReturnAssociatedFilingTypeWhenPassedRandomStringAndHasParentEntityId(
            TransactionKindCriteria kindCriteria, final boolean expected) {
        // given

        // when
        final boolean actual = formTypeService.isAssociatedFilingBlacklisted(kindCriteria);

        // then
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> isAssociatedFilingBlacklistedScenarios() {
        return Stream.of(
                Arguments.of(new TransactionKindCriteria(
                        "12345",
                        "67891",
                        "any string not on blacklist",
                        "",
                        null), false),
                Arguments.of(new TransactionKindCriteria(
                        "12345",
                        "67891",
                        "",
                        "any string not on blacklist",
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
