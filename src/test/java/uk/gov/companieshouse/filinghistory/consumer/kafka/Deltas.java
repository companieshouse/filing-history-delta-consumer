package uk.gov.companieshouse.filinghistory.consumer.kafka;

record Deltas(String entity_id, String javaDelta, String perlDelta) {

}
