package uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.SentenceCase;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Default;

class DefaultPropertiesTest {

    private static final ObjectMapper MAPPER = new YAMLMapper();

    private final TransformerFactory transformerFactory = TransformerTestingUtils.getTransformerFactory();

    private static final String DEFAULT = """
            - default:
                set:
                  matched_default: 1          # Identify cases where no matching rule was found
                                              # Note: data.category will be passed through unchanged
                  data.description: legacy    # Not actually legacy forms but want to display original description in this case
                  data.description_values.description: '[% data.description | sentence_case %]'
                  original_description: '[% data.description | sentence_case %]'
            """;

    @Test
    void shouldCompileDefaultRule() throws JacksonException {

        List<RuleProperties> ruleProperties = MAPPER.readValue(DEFAULT, new TypeReference<>() {
        });
        DefaultProperties defaultProperties = ruleProperties.getFirst().defaultRule();
        Default def = defaultProperties.compile(transformerFactory);

        assertEquals(4, def.setters().size());
        assertEquals("1", def.setters().get("matched_default").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class, def.setters().get("matched_default").transformer());

        assertEquals("legacy", def.setters().get("data.description").arguments().getFirst());
        assertInstanceOf(ReplaceProperty.class,
                def.setters().get("data.description").transformer());

        assertEquals("data.description",
                def.setters().get("data.description_values.description").arguments().getFirst());
        assertInstanceOf(
                SentenceCase.class,
                def.setters().get("data.description_values.description").transformer());

        assertEquals("data.description",
                def.setters().get("original_description").arguments().getFirst());
        assertInstanceOf(
                SentenceCase.class, def.setters().get("original_description").transformer());
    }
}