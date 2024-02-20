package uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.When;

class WhenPropertiesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private static final String ONE_LIKE = """
            - when:
                eq:
                  data.type: TM01
                like:
                  data.description: '^(?i:.*, DIRECTOR (?<officerName>.+?))$'
              then:
                set:
                  data.category: officers
                  data.description: termination-director-company-with-name
                  data.description_values.officer_name: '[% officerName | title_case  %]'
                  data.subcategory: termination
            """;
    private static final String TWO_LIKES = """
            - when:
                eq:
                  data.type: LLAD01
                like:
                  data.description: '^(?i:REGISTERED OFFICE CHANGED ON (?<changeDate>\\d+\\D\\d+\\D\\d+) FROM\\s*(?<oldAddress>.+?))$'
                  original_values.new_ro_address: '^(?<newAddress>.+)$'
              then:
                set:
                  data.category: address
                  data.description: change-registered-office-address-limited-liability-partnership-with-date-old-address-new-address
                  data.description_values.old_address: '[% oldAddress | address_case %]'
                  data.description_values.new_address: '[% newAddress | address_case %]'
                  original_description: '[% data.description | sentence_case %]'
                  data.action_date: '[% change_date | bson_date %]'
                  data.description_values.change_date: '[% changeDate | bson_date %]'
            """;
    private static final String MISSING_EQ = """
            - when:
                like:
                  data.description: '^(?i:.*, DIRECTOR (?<officerName>.+?))$'
              then:
                set:
                  data.category: officers
                  data.description: termination-director-company-with-name
                  data.description_values.officer_name: '[% officerName | title_case  %]'
                  data.subcategory: termination
            """;
    private static final String MISSING_FORM_TYPE = """
            - when:
                eq:
                  stuff: stuff
                like:
                  data.description: '^(?i:.*, DIRECTOR (?<officerName>.+?))$'
              then:
                set:
                  data.category: officers
                  data.description: termination-director-company-with-name
                  data.description_values.officer_name: '[% officerName | title_case  %]'
                  data.subcategory: termination
            """;

    @Test
    void shouldCompileLikeWithOneCaptureGroup() throws JsonProcessingException {

        List<RuleProperties> ruleProperties = MAPPER.readValue(ONE_LIKE, new TypeReference<>() {
        });
        WhenProperties whenProperties = ruleProperties.getFirst().when();
        When when = whenProperties.compile();

        assertEquals("data.type", when.field());
        assertEquals("TM01", when.formType());
        assertEquals(1, when.like().size());
        assertEquals("^(?i:.*, DIRECTOR (?<officerName>.+?))$",
                when.like().get("data.description").pattern());
    }

    @Test
    void shouldCompileLikeWithTwoCaptureGroups() throws JsonProcessingException {

        List<RuleProperties> ruleProperties = MAPPER.readValue(TWO_LIKES, new TypeReference<>() {
        });
        WhenProperties whenProperties = ruleProperties.getFirst().when();
        When when = whenProperties.compile();

        assertEquals("data.type", when.field());
        assertEquals("LLAD01", when.formType());
        assertEquals(2, when.like().size());
        assertEquals(
                "^(?i:REGISTERED OFFICE CHANGED ON (?<changeDate>\\d+\\D\\d+\\D\\d+) FROM\\s*(?<oldAddress>.+?))$",
                when.like().get("data.description").pattern());
        assertEquals("^(?<newAddress>.+)$",
                when.like().get("original_values.new_ro_address").pattern());
    }

    @Test
    void shouldFailToCompileWhenMissingDataTypeField() throws JsonProcessingException {

        List<RuleProperties> ruleProperties = MAPPER.readValue(MISSING_FORM_TYPE,
                new TypeReference<>() {
                });
        WhenProperties whenProperties = ruleProperties.getFirst().when();
        Executable executable = whenProperties::compile;

        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                executable);
        assertEquals("Missing data.type or type data", exception.getMessage());
    }

    @Test
    void shouldFailToCompileWhenMissingEqField() throws JsonProcessingException {

        List<RuleProperties> ruleProperties = MAPPER.readValue(MISSING_EQ, new TypeReference<>() {
        });
        WhenProperties whenProperties = ruleProperties.getFirst().when();
        Executable executable = whenProperties::compile;

        // then
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("Missing eq field", exception.getMessage());
    }
}