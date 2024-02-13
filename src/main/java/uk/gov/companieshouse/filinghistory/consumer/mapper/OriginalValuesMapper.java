package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getFieldValueFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;

@Component
public class OriginalValuesMapper {

    public InternalDataOriginalValues map(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return new InternalDataOriginalValues()
                .resignationDate(getFieldValueFromJsonNode(jsonNode, "resignation_date"))
                .officerName(getFieldValueFromJsonNode(jsonNode, "officer_name"));
    }
}
