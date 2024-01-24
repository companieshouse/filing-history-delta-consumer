package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;

@Component
public class DeltaServiceRouter {

    private final DeltaService upsertDeltaService;

    public DeltaServiceRouter(DeltaService upsertDeltaService) {
        this.upsertDeltaService = upsertDeltaService;
    }

    public void route(ChsDelta delta) {
        if (!delta.getIsDelete()) {
            upsertDeltaService.process(delta);
        }
    }
}
