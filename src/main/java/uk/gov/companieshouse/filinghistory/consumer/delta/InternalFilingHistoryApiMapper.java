package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class InternalFilingHistoryApiMapper {

    public InternalFilingHistoryApi mapJsonNodeToInternalFilingHistoryApi(final JsonNode jsonNode) {
        return new InternalFilingHistoryApi();
    }
}
