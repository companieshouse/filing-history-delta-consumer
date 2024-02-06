package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerTestingUtils;

class SentenceCaseTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private SentenceCase sentenceCase;

    @BeforeEach
    void beforeEach() {
        sentenceCase = new SentenceCase(MAPPER);
    }

    @Test
    void shouldTransformCaptureGroupValueToSentenceCaseAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .putObject("description_values")
                .put("change_details", "Hello, goodbye, etc. greetings");
        // when
        sentenceCase.transform(source, actual, "data.description_values.change_details", List.of("changeDetails"),
                Map.of("changeDetails", "hello, goodbye, etc. greetings"));

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldTransformSourceNodeValueToSentenceCaseAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        source.putObject("data")
                .put("description", "sentence case me");
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected.put("original_description", "Sentence case me");
        expected
                .putObject("data")
                .put("description", "sentence case me"); // this gets overwritten in a different setter
        // when
        sentenceCase.transform(source, actual, "original_description", List.of("data.description"),
                Map.of());

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("sentenceCaseFormatting")
    @DisplayName("Format text as sentence case")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        //given

        //when
        String output = sentenceCase.transformSentenceCase(input);

        //then
        assertEquals(expected, output);
    }

    private static Stream<Arguments> sentenceCaseFormatting() {
        return Stream.of(
                Arguments.of("LAZINESS, IMPATIENCE AND HUBRIS", "Laziness, impatience and hubris"),
                Arguments.of("NOTICE OF STATEMENT OF AFFAIRS/2.14B/2.15B",
                        "Notice of statement of affairs/2.14B/2.15B"), //is description values
                Arguments.of(
                        "TRANSACTION OSTM03- BR000273 PERSON AUTHORISED TO REPRESENT TERMINATED 29/12/2012 NICOLAS THEMISTOCLI",
                        "Transaction OSTM03- BR000273 person authorised to represent terminated 29/12/2012 nicolas themistocli"),
                //should be charge_details.
                Arguments.of("NI011703", "NI011703"),
                Arguments.of(
                        "APPLICATION BY A PUBLIC COMPANY FOR RE-REGISTRATION AS A PRIVATE COMPANY FOLLOWING A COURT ORDER REDUCING CAPITAL",
                        "Application by a public company for re-registration as a private company following a court order reducing capital"),
//                Arguments.of("Inconsistency At the time of filing, this document appeared to be inconsistent with other information filed against the company. A TM01 has been received to terminate David Lawrence as a director from 23/10/09, however, this person is no longer a director on our records.",
//                        "Inconsistency at the time of filing, this document appeared to be inconsistent with other information filed against the company. A TM01 has been received to terminate David Lawrence as a director from 23/10/09, however, this person is no longer a director on our records."),
//                //this one is an interesting case, in the perl it capitalises the At but proper sentence case should be first word capitalised and the other ones not. Also David Lawrence is capitalised properly in the Perl but not here.
                Arguments.of("NOTIFICATION OF A PERSON WITH SIGNIFICANT CONTROL RELEVANT LEGAL ENTITY",
                        "Notification of a person with significant control relevant legal entity"),
                Arguments.of("NOTIFICATION OF A PERSON WITH SIGNIFICANT CONTROL OTHER REGISTRABLE PERSON",
                        "Notification of a person with significant control other registrable person"),
//                Arguments.of("LLP MEMBER APPOINTED MONEY & MORTGAGES [UK] LLP", "LLP member appointed money & mortgages [uk] LLP"),
                Arguments.of("[AMENDED] CERTIFICATE OF CONSTITUTION OF CREDITORS' COMMITTEE",
                        "[Amended] certificate of constitution of creditors' committee"),
                Arguments.of("", ""),
                Arguments.of(null, null),
                Arguments.of(".", "."),
                Arguments.of("a", "A"),
                Arguments.of("æ", "æ"),
                Arguments.of("ab", "Ab"),
                Arguments.of("aB", "Ab"),
                Arguments.of("bread butter", "Bread butter"),
                Arguments.of("bReAD BuTteR", "Bread butter"),
                Arguments.of("i think therefore i am", "I think therefore I am"),
                Arguments.of("\"i am?\"", "\"I am?\""),
                Arguments.of("p/office p/office", "P/Office p/office"),
                Arguments.of("p/!office p/office", "P/!Office p/office"),
                Arguments.of("one. two. three.", "One. Two. Three."),
                Arguments.of("\"i.\" am. error.", "\"I.\" Am. Error."),
                Arguments.of("\"one.\" two. three.", "\"One.\" Two. Three."),
                Arguments.of("hello, goodbye, etc. greetings", "Hello, goodbye, etc. greetings"),
                Arguments.of("one two ) three", "One two ) three"),
                Arguments.of("Once\nupon\na\ntime", "Once upon a time"),
                Arguments.of("d.r. in the house", "D.R. in the house"),
                Arguments.of("\"d.r. in\" the house", "\"D.R. in\" the house"),
                Arguments.of("one  \t two", "One two"),
                Arguments.of("java coffee 4l1f3", "Java coffee 4L1F3"),
                Arguments.of("llp", "LLP"),
                Arguments.of("\"llp\"", "\"LLP\""),
                Arguments.of("d.r.", "D.R."),
                Arguments.of("b.sc", "B.SC"),
                Arguments.of("d.r john smith b.sc of london", "D.r john smith B.SC of london"),
                Arguments.of("d.r john smith b.sci of london", "D.r john smith B.sci of london"),
                Arguments.of("d.r john smith b.sci.b.sci.b.sci. of london",
                        "D.r john smith B.sci.B.sci.B.sci. Of london"),
                Arguments.of("b.sc.", "B.SC."),
                Arguments.of("b.sci.", "B.sci."),
                Arguments.of("a.b.c.d.sci.", "A.B.C.D.sci."),
                Arguments.of("sci.d.c.b.a.", "Sci.D.C.B.A."),
                Arguments.of("the word is sci.d.c.b.a.", "The word is sci.D.C.B.A."),
                Arguments.of("the word is s.ci.d.c.b.a.", "The word is S.ci.D.C.B.A."),
                Arguments.of("the word is; sci.d.c.b.a.", "The word is; sci.D.C.B.A."),
                Arguments.of("b.a.!b.\"sc.m?.a.?m.sc.", "B.A.!B.\"SC.m?.A.?M.SC."),
                Arguments.of("to be. or not to be.", "To be. Or not to be."),
                Arguments.of("£220,000.00                              AND ALL OTHER MONIES DUE OR TO BECOME DUE",
                        "£220,000.00 and all other monies due or to become due"),
                Arguments.of("john smith b.sc. is here", "John smith B.SC. Is here"),
                Arguments.of("i am mr. john smith ba.sci of london", "I am mr. John smith ba.sci of london"),
                Arguments.of("i am mr. john smith b.sci of london", "I am mr. John smith B.sci of london"),
                Arguments.of("harrow-on-the-hill", "Harrow-on-the-hill"),
                Arguments.of("the presenter is from harrow-on-the-hill", "The presenter is from harrow-on-the-hill"),
                Arguments.of("the presenter is from \"harrow\"-\"on-the\"-\"hill!!!",
                        "The presenter is from \"harrow\"-\"on-the\"-\"hill!!!"),
                Arguments.of("don't tell me.", "Don't tell me."),
                Arguments.of("c,om,mas", "C,om,mas"),
                Arguments.of("the trees, the branches and the leaves", "The trees, the branches and the leaves"),
                Arguments.of("2 + 2 = 4", "2 + 2 = 4"),
                Arguments.of("2+2=4", "2+2=4"),
                Arguments.of("this is a word with two full stops.. so is this..",
                        "This is a word with two full stops.. So is this.."),
                Arguments.of("i.. don't know if this will work", "I.. Don't know if this will work"),
                Arguments.of(
                        "p/office the d.r. of an lLp saYs a cAT is ) for ChrIstmAS etc. \n\t but i\tthink (a cat) is 4life! æthelred is ready.",
                        "P/Office the D.R. of an LLP says a cat is ) "
                                + "for christmas etc. but I think (a cat) is 4LIFE! æThelred is ready."),
                Arguments.of(
                        "This sentence contains sequence AB.1234. sentence casing should apply after the full stop",
                        "This sentence contains sequence ab.1234. Sentence casing should apply after the full stop"),
                Arguments.of(
                        "This sentence contains brackets and sequence AB.1234. (sentence casing) applies inside the brackets and after the full stop.",
                        "This sentence contains brackets and sequence ab.1234. (Sentence casing) applies inside the brackets and after the full stop."),
                Arguments.of("(this sentence has closing brackets after a full stop.) this one does not.",
                        "(This sentence has closing brackets after a full stop.) This one does not."),
                Arguments.of("this sentence has an unmatched closing bracket after a full stop.) this one does not.",
                        "This sentence has an unmatched closing bracket after a full stop.) this one does not."),
                Arguments.of("THIS SENTENCE CONTAINS AN ACRONYM 2.2I WITH AN I",
                        "This sentence contains an acronym 2.2I with an I"),
                Arguments.of("this sentence contains approximately (i)-(iii) roman numerals",
                        "This sentence contains approximately (i)-(iii) roman numerals")

        );
    }
}
