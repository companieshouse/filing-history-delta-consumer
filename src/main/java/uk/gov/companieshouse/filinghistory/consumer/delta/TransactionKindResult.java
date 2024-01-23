package uk.gov.companieshouse.filinghistory.consumer.delta;

import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

public record TransactionKindResult(String encodedId, TransactionKindEnum kind) {

}