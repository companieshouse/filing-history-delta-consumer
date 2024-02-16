package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LowerCaseTest {

    private LowerCase lowerCase;

    @BeforeEach
    void beforeEach() {
        lowerCase = new LowerCase();
    }

    @ParameterizedTest
    @MethodSource("lowerCaseFormatting")
    @DisplayName("Format text as lower case")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        //given

        //when
        String output = lowerCase.transformLowerCase(input);

        //then
        assertEquals(expected, output);
    }

    private static Stream<Arguments> lowerCaseFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("MEDIUM", "medium"),
                Arguments.of("SMALL", "small"),
                Arguments.of("FULL", "full"),
                Arguments.of("INITIAL", "initial")
        );
    }
}
