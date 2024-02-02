package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.delta.ChsDelta;
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

    @ParameterizedTest
    @CsvSource({
            "TM01"
    })
    void shouldSuccessfullyPassDeserialisedAndMappedDeltaToApiClient(final String prefix) throws Exception {
        // given
        when(deserialiser.deserialiseFilingHistoryDelta(any())).thenReturn(delta);
        when(mapper.processDelta(any(), anyString())).thenReturn(apiRequest);

        final String jsonString = IOUtils.resourceToString("/%s_delta.json".formatted(prefix), StandardCharsets.UTF_8);

        ChsDelta chsDelta = new ChsDelta(jsonString, 0, "contextId", false);

        // when
        service.process(chsDelta);

        // then
        verify(deserialiser).deserialiseFilingHistoryDelta(jsonString);
        verify(mapper).processDelta(delta, "contextId");
        verify(apiClient).upsertFilingHistory(apiRequest);
    }
}