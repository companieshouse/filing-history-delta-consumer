package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.SentenceCase;

class SentenceCaseTransformerTest {


    private SentenceCase sentenceCase;

    @BeforeEach
    void beforeEach() {
        sentenceCase = new SentenceCase();
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("sentenceCaseFormatting")
    @DisplayName("Format text as sentence case")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected){
        //given

        //when
        String output = sentenceCase.transformSentenceCase(input);

        //then
        assertEquals(expected, output);
        System.out.println(output);
    }

    private static Stream<Arguments> sentenceCaseFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("a", "A")
        );
    }
}
