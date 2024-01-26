package uk.gov.companieshouse.filinghistory.consumer.delta;

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
import uk.gov.companieshouse.filinghistory.consumer.serdes.FilingHistoryDeltaDeserialiser;

@ExtendWith(MockitoExtension.class)
class UpsertDeltaServiceTest {

    private static final String DELTA_JSON = "json string representing filing history delta";

    @InjectMocks
    private UpsertDeltaService service;
    @Mock
    private FilingHistoryDeltaDeserialiser deserialiser;
    @Mock
    private FilingHistoryDeltaMapper mapper;
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
        when(mapper.map(any(), anyString())).thenReturn(apiRequest);

        ChsDelta chsDelta = new ChsDelta(DELTA_JSON, 0, "contextId", false);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseFilingHistoryDelta(DELTA_JSON);
        verify(mapper).map(delta, "contextId");
        verify(apiClient).upsertFilingHistory(apiRequest);
    }
}