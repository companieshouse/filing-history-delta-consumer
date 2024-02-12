package uk.gov.companieshouse.filinghistory.consumer.service;

import uk.gov.companieshouse.delta.ChsDelta;

public interface DeltaService {

    void process(ChsDelta delta);
}
