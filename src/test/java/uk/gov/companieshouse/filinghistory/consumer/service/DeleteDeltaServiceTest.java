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

    private static final String DELTA_DATA = "delta";
    private static final TransactionKindResult KIND_RESULT =
            new TransactionKindResult("ABCD1234EFGH", TransactionKindEnum.ANNOTATION);
    private static final DeleteApiClientRequest API_CLIENT_REQUEST =
            new DeleteApiClientRequest("ABCD1234EFGH", "12345678",
                    "98765432", "20240219123045999999");

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
        when(delta.getDeltaAt()).thenReturn("20240219123045999999");
        when(delta.getEntityId()).thenReturn("98765432");
        when(delta.getCompanyNumber()).thenReturn("12345678");

        ChsDelta chsDelta = new ChsDelta(DELTA_DATA, 0, "contextId", true);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseFilingHistoryDeleteDelta(DELTA_DATA);
        verify(kindService).encodeIdByTransactionKind(any());
        verify(apiClient).deleteFilingHistory(API_CLIENT_REQUEST);
    }
}