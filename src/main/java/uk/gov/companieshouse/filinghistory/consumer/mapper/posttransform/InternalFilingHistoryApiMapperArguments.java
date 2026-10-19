package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindResult;

public record InternalFilingHistoryApiMapperArguments(JsonNode topLevelNode, TransactionKindResult kindResult,
                                                      String companyNumber, String deltaAt, String updatedBy) {

}
