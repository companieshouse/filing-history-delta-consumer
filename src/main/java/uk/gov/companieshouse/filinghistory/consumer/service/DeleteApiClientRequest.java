package uk.gov.companieshouse.filinghistory.consumer.service;

public record DeleteApiClientRequest(String transactionId, String companyNumber, String entityId, String deltaAt) {

}
