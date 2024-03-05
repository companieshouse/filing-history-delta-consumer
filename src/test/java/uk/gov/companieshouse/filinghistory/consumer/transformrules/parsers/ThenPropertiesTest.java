package uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.SentenceCase;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Then;

class ThenPropertiesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final TransformerFactory transformerFactory = TransformerTestingUtils.getTransformerFactory();

    private static final String THEN_WITH_DEFINE_AND_EXEC = """
            - when:
                eq:
                  type: TEST01
                like:
                  data.description: '^(?i)(?<date>\\d+\\D\\d+\\D\\d+) STATEMENT OF CAPITAL;(?:\\w+) (?:\\d+\\.\\d+|\\.\\d+|\\d+)'
              then:
                define:
                  extract: '(?i:\\bSTATEMENT OF CAPITAL;(?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))'
                set:
                  data.description: statement-of-capital
                exec:
                  process_capital: [ '[% data.description %]', '[% extract %]' ]
            """;

    private static final String THEN_WITH_TWO_DEFINE_ELEMENTS = """
            - when:
                eq:
                  data.type: TEST01
                like:
                  data.description: '^(?i:(?<date>\\d+\\D\\d+\\D\\d+) (?:(?:STATEMENT OF CAPITAL)|(?:TREASURY CAPITAL)) \\w+ (?:\\d+\\.\\d+|\\.\\d+|\\d+))'
              then:
                define:
                  extract: '(?i:(?<capitalDate>\\d+\\D\\d+\\D\\d+) (?:(?<capitalDesc>STATEMENT OF CAPITAL)|(?<capitalAltDesc>TREASURY CAPITAL)) (?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))'
                  alt_description: capital-cancellation-treasury-shares-with-date-treasury-capital-figure
                set:
                  data.description: capital-cancellation-treasury-shares-with-date-treasury-capital-figure
                exec:
                  process_capital: [ '[% data.description %]', '[% extract %]', '[% altDescription %]' ]
            """;

    private static final String THEN_WITH_PLACEHOLDER_FUNCTION = """
            - when:
                eq:
                  data.type: 4.20
              then:
                set:
                  data.category: insolvency
                  data.description: liquidation-voluntary-statement-of-affairs
                  data.subcategory: voluntary
                  original_description: '[% data.description | sentence_case %]'
                  """;

    private static final String THEN_WITH_PLACEHOLDER_CAPTURE_GROUP_NO_FUNCTION = """
            - when:
                eq:
                  data.type: LIQ02
                like:
                  data.description: '^(?i:NOTICE OF STATEMENT OF AFFAIRS BY LIQUIDATOR/DIRECTOR IN CVL/(?<formAttached>.+?):.*)$'
              then:
                set:
                  data.category: insolvency
                  data.description: liquidation-voluntary-statement-of-affairs-with-form-attached
                  data.description_values.form_attached: '[% formAttached %]'
                  data.subcategory: voluntary
                  original_description: '[% data.description | sentence_case %]'
                  """;

    @Test
    void shouldCompileWithOneDefineClause() throws JsonProcessingException {
        List<RuleProperties> ruleProperties = MAPPER.readValue(THEN_WITH_DEFINE_AND_EXEC,
                new TypeReference<>() {
                });
        ThenProperties thenProperties = ruleProperties.getFirst().then();
        Then then = thenProperties.compile(transformerFactory);

        Pattern expected = Pattern.compile(
                "(?i:\\bSTATEMENT OF CAPITAL;(?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
        assertEquals(expected.pattern(), then.execArgs().extract().pattern());
        assertNull(then.execArgs().altDescription());

        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.description").transformer());
        assertEquals("statement-of-capital",
                then.setters().get("data.description").arguments().getFirst());
    }

    @Test
    void shouldCompileWithTwoDefineClauses() throws JsonProcessingException {
        List<RuleProperties> ruleProperties = MAPPER.readValue(THEN_WITH_TWO_DEFINE_ELEMENTS,
                new TypeReference<>() {
                });
        ThenProperties thenProperties = ruleProperties.getFirst().then();
        Then then = thenProperties.compile(transformerFactory);

        Pattern expected = Pattern.compile(
                "(?i:(?<capitalDate>\\d+\\D\\d+\\D\\d+) (?:(?<capitalDesc>STATEMENT OF CAPITAL)|(?<capitalAltDesc>TREASURY CAPITAL)) (?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
        assertEquals(expected.pattern(), then.execArgs().extract().pattern());
        assertEquals(
                "capital-cancellation-treasury-shares-with-date-treasury-capital-figure",
                then.execArgs().altDescription());

        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.description").transformer());
        assertEquals("capital-cancellation-treasury-shares-with-date-treasury-capital-figure",
                then.setters().get("data.description").arguments().getFirst());
    }

    @Test
    void shouldCompileWithPlaceholderFunction() throws JsonProcessingException {
        List<RuleProperties> ruleProperties = MAPPER.readValue(THEN_WITH_PLACEHOLDER_FUNCTION,
                new TypeReference<>() {
                });
        ThenProperties thenProperties = ruleProperties.getFirst().then();
        Then then = thenProperties.compile(transformerFactory);

        assertNull(then.execArgs());

        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.description").transformer());
        assertEquals("liquidation-voluntary-statement-of-affairs",
                then.setters().get("data.description").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.category").transformer());
        assertEquals("insolvency", then.setters().get("data.category").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.subcategory").transformer());
        assertEquals("voluntary", then.setters().get("data.subcategory").arguments().getFirst());
        assertInstanceOf(SentenceCase.class,
                then.setters().get("original_description").transformer());
        assertEquals("data.description",
                then.setters().get("original_description").arguments().getFirst());
    }

    @Test
    void shouldCompileWithPlaceholderCaptureGroupButNoFunction() throws JsonProcessingException {
        List<RuleProperties> ruleProperties = MAPPER.readValue(THEN_WITH_PLACEHOLDER_CAPTURE_GROUP_NO_FUNCTION,
                new TypeReference<>() {
                });
        ThenProperties thenProperties = ruleProperties.getFirst().then();
        Then then = thenProperties.compile(transformerFactory);

        assertNull(then.execArgs());

        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.description").transformer());
        assertEquals("liquidation-voluntary-statement-of-affairs-with-form-attached",
                then.setters().get("data.description").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.category").transformer());
        assertEquals("insolvency", then.setters().get("data.category").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.description_values.form_attached").transformer());
        assertEquals("formAttached",
                then.setters().get("data.description_values.form_attached").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class,
                then.setters().get("data.subcategory").transformer());
        assertEquals("voluntary", then.setters().get("data.subcategory").arguments().getFirst());
        assertInstanceOf(SentenceCase.class,
                then.setters().get("original_description").transformer());
        assertEquals("data.description",
                then.setters().get("original_description").arguments().getFirst());
    }
}