package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@Component
public class UpsertDeltaService implements DeltaService {

    private final FilingHistoryDeltaDeserialiser deserialiser;
    private final FilingHistoryDeltaMapper mapper;
    private final FilingHistoryApiClient apiClient;

    public UpsertDeltaService(FilingHistoryDeltaDeserialiser deserialiser, FilingHistoryDeltaMapper mapper,
            FilingHistoryApiClient apiClient) {
        this.deserialiser = deserialiser;
        this.mapper = mapper;
        this.apiClient = apiClient;
    }

    @Override
    public void process(ChsDelta delta) {
        FilingHistoryDelta filingHistory = deserialiser.deserialiseFilingHistoryDelta(delta.getData());
        InternalFilingHistoryApi apiRequest = mapper.map(filingHistory);
        apiClient.upsertFilingHistory(apiRequest);
    }
}
