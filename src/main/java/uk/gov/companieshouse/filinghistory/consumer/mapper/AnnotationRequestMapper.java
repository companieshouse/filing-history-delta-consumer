package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class AnnotationRequestMapper implements ChildRequestMapper {
    @Override
    public InternalFilingHistoryApi map(JsonNode dataNode) {
        return null;
    }
}
