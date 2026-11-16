package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import tools.jackson.databind.JsonNode;
import uk.gov.companieshouse.filinghistory.consumer.service.TransactionKindResult;

public record InternalFilingHistoryApiMapperArguments(JsonNode topLevelNode, TransactionKindResult kindResult,
                                                      String companyNumber, String deltaAt, String updatedBy) {

}
