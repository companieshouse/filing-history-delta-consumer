package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;

class OriginalValuesMapperTest {

    private final static String RESIGNATION_DATE = "03/02/2013";
    private final static String OFFICER_NAME = "John Tester";

    private final OriginalValuesMapper originalValuesMapper = new OriginalValuesMapper();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .registerModule(new JavaTimeModule());

    @Test
    void shouldMapOriginalValuesFromJsonNode() {
        // given
        final JsonNode jsonNode = objectMapper.createObjectNode()
                .putObject("original_values")
                .put("resignation_date", RESIGNATION_DATE)
                .put("officer_name", OFFICER_NAME);

        final InternalDataOriginalValues expected = new InternalDataOriginalValues()
                .resignationDate(RESIGNATION_DATE)
                .officerName(OFFICER_NAME);

        // when
        final InternalDataOriginalValues actual = originalValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapNullValuesIfJsonNodeFieldsAreNull() {
        // given
        final JsonNode jsonNode = objectMapper.createObjectNode();

        final InternalDataOriginalValues expected = new InternalDataOriginalValues()
                .resignationDate(null)
                .officerName(null);

        // when
        final InternalDataOriginalValues actual = originalValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
        assertNull(jsonNode.get("original_values"));
    }

    @Test
    void shouldReturnNullIfJsonNodeIsNull() {
        // given

        // when
        final InternalDataOriginalValues actual = originalValuesMapper.map(null);

        // then
        assertNull(actual);
    }
}
