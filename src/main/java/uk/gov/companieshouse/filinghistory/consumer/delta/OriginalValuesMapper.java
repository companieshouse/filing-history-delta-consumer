package uk.gov.companieshouse.filinghistory.consumer.delta;

import static uk.gov.companieshouse.filinghistory.consumer.delta.MappingUtils.getValueFromField;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;

@Component
public class OriginalValuesMapper {

    public InternalDataOriginalValues map(JsonNode jsonNode) {
        return new InternalDataOriginalValues()
                .resignationDate(getValueFromField(jsonNode, "resignation_date"))
                .officerName(getValueFromField(jsonNode, "officer_name"));
    }
}
