package uk.gov.companieshouse.filinghistory.consumer.delta;

import uk.gov.companieshouse.delta.ChsDelta;

public interface Service {

    void process(ChsDelta delta);
}
