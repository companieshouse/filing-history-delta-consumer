package uk.gov.companieshouse.filinghistory.consumer.service;

import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

public record TransactionKindResult(String encodedId, TransactionKindEnum kind) {

}