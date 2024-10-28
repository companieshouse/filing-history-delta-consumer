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
    private final TransactionKindService kindService;

    public DeleteDeltaService(FilingHistoryDeltaDeserialiser deserialiser, FilingHistoryApiClient apiClient,
            TransactionKindService kindService) {
        this.deserialiser = deserialiser;
        this.apiClient = apiClient;
        this.kindService = kindService;
    }

    @Override
    public void process(ChsDelta delta) {
        FilingHistoryDeleteDelta deleteDelta = deserialiser.deserialiseFilingHistoryDeleteDelta(delta.getData());

        TransactionKindCriteria criteria = new TransactionKindCriteria(
                deleteDelta.getEntityId(),
                deleteDelta.getParentEntityId(),
                deleteDelta.getFormType(),
                deleteDelta.getParentFormType(),
                deleteDelta.getBarcode());

        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);
        DeleteApiClientRequest clientRequest = new DeleteApiClientRequest(kindResult.encodedId(),
                deleteDelta.getCompanyNumber(), deleteDelta.getEntityId(), deleteDelta.getDeltaAt());
        apiClient.deleteFilingHistory(clientRequest);
    }
}
