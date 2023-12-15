package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;

@Component
public class FilingHistoryService implements Service {

    @Override
    public void process(ChsDelta delta) {
        // do nothing
    }
}
