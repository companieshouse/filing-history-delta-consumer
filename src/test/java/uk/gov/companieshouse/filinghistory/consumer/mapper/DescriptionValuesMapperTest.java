package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.consumer.mapper.DescriptionValuesMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class DescriptionValuesMapperTest {

    private static final String OFFICER_NAME = "John Tester";
    private static final String TERMINATION_DATE = "06/05/2013";
    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private final DescriptionValuesMapper descriptionValuesMapper = new DescriptionValuesMapper();


    @Test
    void shouldMapFilingHistoryItemDataDescriptionValuesObject() {
        // given
        final JsonNode jsonNode = MAPPER.createObjectNode()
                .putObject("description_values")
                .put("officer_name", OFFICER_NAME)
                .put("termination_date", TERMINATION_DATE);

        final FilingHistoryItemDataDescriptionValues expected = new FilingHistoryItemDataDescriptionValues()
                .officerName(OFFICER_NAME)
                .terminationDate(TERMINATION_DATE);

        // when
        final FilingHistoryItemDataDescriptionValues actual = descriptionValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFilingHistoryItemDataDescriptionValuesObjectWhenFieldsAreEmpty() {
        // given
        final JsonNode jsonNode = MAPPER.createObjectNode();

        final FilingHistoryItemDataDescriptionValues expected = new FilingHistoryItemDataDescriptionValues()
                .officerName(null)
                .terminationDate(null);

        // when
        final FilingHistoryItemDataDescriptionValues actual = descriptionValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
        assertNull(jsonNode.get("description_values"));
    }

    @Test
    void shouldReturnNullIfJsonNodeIsNull() {
        // given

        // when
        final FilingHistoryItemDataDescriptionValues actual = descriptionValuesMapper.map(null);

        // then
        assertNull(actual);
    }
}
