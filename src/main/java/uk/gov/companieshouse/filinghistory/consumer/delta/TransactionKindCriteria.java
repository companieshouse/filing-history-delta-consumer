package uk.gov.companieshouse.filinghistory.consumer.delta;

public record TransactionKindCriteria(String entityId, String parentEntityId, String formType, String parentFormType, String barcode) {

}
