package uk.gov.companieshouse.filinghistory.consumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDeleteDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.apiclient.FilingHistoryApiClient;
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@Component
public class DeleteDeltaService implements DeltaService {

    private final FilingHistoryDeltaDeserialiser deserialiser;
    private final FilingHistoryApiClient apiClient;

    public DeleteDeltaService(FilingHistoryDeltaDeserialiser deserialiser, FilingHistoryApiClient apiClient) {
        this.deserialiser = deserialiser;
        this.apiClient = apiClient;
    }

    @Override
    public void process(ChsDelta delta) {
        FilingHistoryDeleteDelta deleteDelta = deserialiser.deserialiseFilingHistoryDeleteDelta(delta.getData());
        apiClient.deleteFilingHistory(deleteDelta.getEntityId());
    }
}
