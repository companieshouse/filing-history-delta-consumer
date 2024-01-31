package uk.gov.companieshouse.filinghistory.consumer.delta;

import static uk.gov.companieshouse.filinghistory.consumer.delta.MappingUtils.getFieldValueFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

@Component
public class DescriptionValuesMapper {

    public FilingHistoryItemDataDescriptionValues map(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return new FilingHistoryItemDataDescriptionValues()
                .officerName(getFieldValueFromJsonNode(jsonNode, "officer_name"))
                .terminationDate(getFieldValueFromJsonNode(jsonNode, "termination_date"));
    }
}
