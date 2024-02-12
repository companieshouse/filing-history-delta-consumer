package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.filinghistory.consumer.service.TransactionKindResult;

public record InternalFilingHistoryApiMapperArguments(JsonNode topLevelNode, TransactionKindResult kindResult,
                                                      String companyNumber, String deltaAt, String updatedBy) {

}
