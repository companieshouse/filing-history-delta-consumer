package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

@Component
public class DescriptionValuesMapper {

    public FilingHistoryItemDataDescriptionValues map(JsonNode topLevelNode) {
        return null;
    }
}
