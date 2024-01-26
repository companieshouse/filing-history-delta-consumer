package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

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
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.AddressCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.BsonDate;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.LowerCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ProcessCapital;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.SentenceCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TitleCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Then;

class ThenPropertiesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private final TransformerFactory transformerFactory = new TransformerFactory(new AddressCase(),
            new BsonDate(), new LowerCase(), new SentenceCase(), new TitleCase(),
            new ReplaceProperty(), new ProcessCapital());

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
}