package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.FilingHistoryDeleteDelta;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.apiclient.FilingHistoryApiClient;
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class DeleteDeltaServiceTest {

    private static final String CONTEXT_ID = "context_id";
    private static final String TRANSACTION_ID = "ABCD1234EFGH";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String ENTITY_ID = "98765432";
    private static final String DELTA_AT = "20240219123045999999";
    private static final String PARENT_ENTITY_ID = "88765432";
    private static final String DELTA_DATA = "delta";
    private static final TransactionKindResult KIND_RESULT =
            new TransactionKindResult(TRANSACTION_ID, TransactionKindEnum.ANNOTATION);
    private static final DeleteApiClientRequest API_CLIENT_REQUEST =
            DeleteApiClientRequest.builder()
                    .transactionId(TRANSACTION_ID)
                    .companyNumber(COMPANY_NUMBER)
                    .entityId(ENTITY_ID)
                    .deltaAt(DELTA_AT)
                    .parentEntityId(PARENT_ENTITY_ID)
                    .build();

    @InjectMocks
    private DeleteDeltaService service;
    @Mock
    private FilingHistoryDeltaDeserialiser deserialiser;
    @Mock
    private FilingHistoryApiClient apiClient;
    @Mock
    private TransactionKindService kindService;
    @Mock
    private FilingHistoryDeleteDelta delta;

    @Test
    void shouldSuccessfullyPassDeserialisedDeleteDeltaToApiClient() {
        // given
        when(deserialiser.deserialiseFilingHistoryDeleteDelta(any())).thenReturn(delta);
        when(kindService.encodeIdByTransactionKind(any())).thenReturn(KIND_RESULT);
        when(delta.getDeltaAt()).thenReturn(DELTA_AT);
        when(delta.getEntityId()).thenReturn(ENTITY_ID);
        when(delta.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(delta.getParentEntityId()).thenReturn(PARENT_ENTITY_ID);

        ChsDelta chsDelta = new ChsDelta(DELTA_DATA, 0, CONTEXT_ID, true);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseFilingHistoryDeleteDelta(DELTA_DATA);
        verify(kindService).encodeIdByTransactionKind(any());
        verify(apiClient).deleteFilingHistory(API_CLIENT_REQUEST);
    }
}