package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;

@Component
public class OriginalValuesMapper {

    public InternalDataOriginalValues map(JsonNode topLevelNode) {
        return null;
    }
}
