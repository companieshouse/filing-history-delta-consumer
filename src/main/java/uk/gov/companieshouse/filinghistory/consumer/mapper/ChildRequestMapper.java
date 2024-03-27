package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface ChildRequestMapper {

    InternalFilingHistoryApi map(JsonNode dataNode);
}
