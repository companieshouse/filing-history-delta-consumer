package uk.gov.companieshouse.filinghistory.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.apiclient.FilingHistoryApiClient;
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@Component
public class UpsertDeltaService implements DeltaService {

    private final FilingHistoryDeltaDeserialiser deserialiser;
    private final FilingHistoryDeltaProcessor filingHistoryDeltaProcessor;
    private final FilingHistoryApiClient apiClient;

    public UpsertDeltaService(FilingHistoryDeltaDeserialiser deserialiser, FilingHistoryDeltaProcessor filingHistoryDeltaProcessor,
            FilingHistoryApiClient apiClient) {
        this.deserialiser = deserialiser;
        this.filingHistoryDeltaProcessor = filingHistoryDeltaProcessor;
        this.apiClient = apiClient;
    }

    @Override
    public void process(ChsDelta delta) {
        FilingHistoryDelta filingHistory = deserialiser.deserialiseFilingHistoryDelta(delta.getData());
        InternalFilingHistoryApi apiRequest = filingHistoryDeltaProcessor.processDelta(filingHistory, delta.getContextId());
        apiClient.upsertFilingHistory(apiRequest);
    }
}
