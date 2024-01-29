package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

class DescriptionValuesMapperTest {

    private static final String OFFICER_NAME = "John Tester";
    private static final String TERMINATION_DATE = "06/05/2013";

    private final DescriptionValuesMapper descriptionValuesMapper = new DescriptionValuesMapper();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .registerModule(new JavaTimeModule());

    @Test
    void shouldMapFilingHistoryItemDataDescriptionValuesObject() {
        // given
        final JsonNode jsonNode = objectMapper.createObjectNode()
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
        final JsonNode jsonNode = objectMapper.createObjectNode()
                .putObject("description_values");

        final FilingHistoryItemDataDescriptionValues expected = new FilingHistoryItemDataDescriptionValues()
                .officerName(null)
                .terminationDate(null);

        // when
        final FilingHistoryItemDataDescriptionValues actual = descriptionValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
    }
}
