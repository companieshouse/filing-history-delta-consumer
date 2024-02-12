package uk.gov.companieshouse.filinghistory.consumer.service;

public record TransactionKindCriteria(String entityId, String parentEntityId, String formType, String parentFormType, String barcode) {

}
