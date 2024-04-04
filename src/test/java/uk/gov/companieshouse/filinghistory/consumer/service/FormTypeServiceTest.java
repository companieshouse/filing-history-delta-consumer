package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FormTypeServiceTest {

    @Autowired
    private FormTypeService formTypeService;

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
                        null), false)
        );
    }
}
