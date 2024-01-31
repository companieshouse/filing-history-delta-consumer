package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;

public record InternalFilingHistoryApiMapperArguments(JsonNode topLevelNode, TransactionKindResult kindResult,
                                                      String companyNumber, String deltaAt, String updatedBy) {

}
