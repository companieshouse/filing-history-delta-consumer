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
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.apiclient.FilingHistoryApiClient;
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class DeleteDeltaServiceTest {

    private static final String ENTITY_ID = "entityId";
    private static final String DELTA_DATA = "delta";

    @InjectMocks
    private DeleteDeltaService service;
    @Mock
    private FilingHistoryDeltaDeserialiser deserialiser;
    @Mock
    private FilingHistoryApiClient apiClient;
    @Mock
    private FilingHistoryDeleteDelta delta;

    @Test
    void shouldSuccessfullyPassDeserialisedDeleteDeltaToApiClient() {
        // given
        when(deserialiser.deserialiseFilingHistoryDeleteDelta(any())).thenReturn(delta);
        when(delta.getEntityId()).thenReturn(ENTITY_ID);

        ChsDelta chsDelta = new ChsDelta(DELTA_DATA, 0, "contextId", true);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseFilingHistoryDeleteDelta(DELTA_DATA);
        verify(apiClient).deleteFilingHistory(ENTITY_ID);
    }
}