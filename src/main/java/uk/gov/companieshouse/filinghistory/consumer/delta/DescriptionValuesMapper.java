package uk.gov.companieshouse.filinghistory.consumer.delta;

import static uk.gov.companieshouse.filinghistory.consumer.delta.MappingUtils.getValueFromField;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

@Component
public class DescriptionValuesMapper {

    public FilingHistoryItemDataDescriptionValues map(final JsonNode jsonNode) {
        return new FilingHistoryItemDataDescriptionValues()
                .officerName(getValueFromField(jsonNode, "officer_name"))
                .terminationDate(getValueFromField(jsonNode, "termination_date"));
    }
}
