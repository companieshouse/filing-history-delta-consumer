package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.apiclient.FilingHistoryApiClient;
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class UpsertDeltaServiceTest {

    @InjectMocks
    private UpsertDeltaService service;
    @Mock
    private FilingHistoryDeltaDeserialiser deserialiser;
    @Mock
    private FilingHistoryDeltaProcessor mapper;
    @Mock
    private FilingHistoryApiClient apiClient;

    @Mock
    private FilingHistoryDelta delta;
    @Mock
    private InternalFilingHistoryApi apiRequest;

    @Test
    void shouldSuccessfullyPassDeserialisedAndMappedDeltaToApiClient() {
        // given
        when(deserialiser.deserialiseFilingHistoryDelta(any())).thenReturn(delta);
        when(mapper.processDelta(any(), anyString())).thenReturn(apiRequest);

        ChsDelta chsDelta = new ChsDelta("delta", 0, "contextId", false);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseFilingHistoryDelta("delta");
        verify(mapper).processDelta(delta, "contextId");
        verify(apiClient).upsertFilingHistory(apiRequest);
    }
}