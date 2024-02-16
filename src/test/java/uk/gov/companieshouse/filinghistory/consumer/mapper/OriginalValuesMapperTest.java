package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class OriginalValuesMapperTest {

    private final static String RESIGNATION_DATE = "03/02/2013";
    private final static String OFFICER_NAME = "John Tester";
    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private final OriginalValuesMapper originalValuesMapper = new OriginalValuesMapper();


    @Test
    void shouldMapOriginalValuesFromJsonNode() {
        // given
        final JsonNode jsonNode = MAPPER.createObjectNode()
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
        final JsonNode jsonNode = MAPPER.createObjectNode();

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
