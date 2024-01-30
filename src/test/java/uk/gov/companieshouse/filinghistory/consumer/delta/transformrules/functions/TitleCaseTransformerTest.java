package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TitleCase;

class TitleCaseTransformerTest {

    private TitleCase titleCase;

    @BeforeEach
    void beforeEach() {
        titleCase = new TitleCase();
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("titleCaseFormatting")
    @DisplayName("Format text as an title case")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected){
        //given

        //when
        String output = titleCase.transformTitleCase(input);

        //then
        assertEquals(expected, output);
    }

    //need these to be parameters from the actual data we are going to feed in like from CIDEV, this seems to be just what Doug could think of before.
    private static Stream<Arguments> titleCaseFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("a", "A"),
                Arguments.of("æ", "Æ"),
                Arguments.of("ab", "Ab"),
                Arguments.of("aB", "Ab"),
                Arguments.of("bread butter", "Bread Butter"),
                Arguments.of("bReAD BuTteR", "Bread Butter"),
                Arguments.of("bread and butter", "Bread and Butter"),
                Arguments.of("and or the", "And or The"),
                Arguments.of("King (of in the) Hill", "King (Of in The) Hill"),
                Arguments.of("King (.of in the) Hill", "King (.of in The) Hill"),
                Arguments.of("King .(of in the) Hill", "King .(Of in The) Hill"),
                Arguments.of("King .(.of in the.). Hill", "King .(.of in the.). Hill"),
                Arguments.of("King (is king of the) Hill", "King (Is King of The) Hill"),
                Arguments.of("King (of. in the) Hill", "King (Of. in The) Hill"),
                Arguments.of("An apple; an orange", "An Apple; An Orange"),
                Arguments.of("An apple; \"an orange", "An Apple; \"an Orange"),
                Arguments.of("An apple; and; an orange", "An Apple; And; an Orange"),
                Arguments.of("java coffee 4l1f3", "Java Coffee 4L1F3"),
                Arguments.of("java coffee \"4l1f3\"", "Java Coffee \"4L1F3\""),
                Arguments.of("d.r.", "D.R."),
                Arguments.of("b.sci.", "B.Sci."),
                Arguments.of("a.b.c.d.sci.", "A.B.C.D.Sci."),
                Arguments.of("sci.d.c.b.a.", "Sci.D.C.B.A."),
                Arguments.of("the word is sci.d.c.b.a.", "The Word is Sci.D.C.B.A."),
                Arguments.of("the word is; sci.d.c.b.a.", "The Word is; Sci.D.C.B.A."), // stop words surrounded with punctuation must not be capitalised
                Arguments.of("the word is s.ci.d.c.b.a.", "The Word is S.Ci.D.C.B.A."),
                Arguments.of("harrow-on-the-hill", "Harrow-on-the-Hill"), // delimited stop words must not be capitalised
                Arguments.of("the presenter is from harrow-on-the-hill", "The Presenter is from Harrow-on-the-Hill"),
                Arguments.of("the presenter is from \"harrow\"-\"on-the\"-\"hill!!!", "The Presenter is from \"Harrow\"-\"on-the\"-\"Hill!!!"),
                Arguments.of("don't tell me.", "Don't Tell Me."),
                Arguments.of("c,om,mas", "C,Om,Mas"),
                Arguments.of("the trees, the branches and the leaves", "The Trees, the Branches and the Leaves"),
                Arguments.of("2 + 2 = 4", "2 + 2 = 4"),
                Arguments.of("2+2=4", "2+2=4"),
                Arguments.of("10/12/12 STATEMENT OF CAPITAL;GBP 50000", "10/12/12 Statement of Capital;Gbp 50000")
        );
    }

}
