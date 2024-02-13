package uk.gov.companieshouse.filinghistory.consumer.service;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeltaServiceRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final DeltaService upsertDeltaService;

    public DeltaServiceRouter(DeltaService upsertDeltaService) {
        this.upsertDeltaService = upsertDeltaService;
    }

    public void route(ChsDelta delta) {
        if (!delta.getIsDelete()) {
            upsertDeltaService.process(delta);
        } else {
            LOGGER.error("Delete process is not yet implemented", DataMapHolder.getLogMap());
            throw new NonRetryableException("Delete process is not yet implemented");
        }
    }
}
