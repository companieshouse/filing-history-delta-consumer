package uk.gov.companieshouse.filinghistory.consumer.kafka;

record PerlDocuments(
        String entityId,
        String transactionId,
        String formType,
        String companyNumber,
        String perlDelta,
        String javaDelta,
        String perlDocument,
        String perlGetSingleResponse,
        String perlGetListResponse) {

}
