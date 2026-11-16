package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class JsonNodeCleanerTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private JsonNodeCleaner jsonNodeCleaner;

    @BeforeEach
    void setUp() {
        jsonNodeCleaner = new JsonNodeCleaner(MAPPER);
    }

    @Test
    void shouldChangeEmptyStringValuesToNull() {
        // given
        JsonNode inputNode = MAPPER.createObjectNode()
                .put("description", "");

        JsonNode expected = MAPPER.createObjectNode()
                .putNull("description");

        // when
        JsonNode actual = jsonNodeCleaner.setEmptyStringsToNull(inputNode);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldRemainUnchangedIfNoEmptyString() {
        // given
        JsonNode inputNode = MAPPER.createObjectNode()
                .put("description", "description");

        // when
        JsonNode actual = jsonNodeCleaner.setEmptyStringsToNull(inputNode);

        // then
        assertEquals(inputNode, actual);
    }
}

