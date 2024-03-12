package uk.gov.companieshouse.filinghistory.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;

@Component
public class DeltaServiceRouter {

    private final DeltaService upsertDeltaService;
    private final DeltaService deleteDeltaService;

    public DeltaServiceRouter(UpsertDeltaService upsertDeltaService, DeleteDeltaService deleteDeltaService) {
        this.upsertDeltaService = upsertDeltaService;
        this.deleteDeltaService = deleteDeltaService;
    }

    public void route(ChsDelta delta) {
        if (!delta.getIsDelete()) {
            upsertDeltaService.process(delta);
        } else {
            deleteDeltaService.process(delta);
        }
    }
}
