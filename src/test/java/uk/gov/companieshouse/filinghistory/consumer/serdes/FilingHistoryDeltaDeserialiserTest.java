package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.FilingHistoryDeleteDelta;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeltaDeserialiserTest {

    public static final String FILING_HISTORY_DELTA = "filing history delta json string";
    public static final String FILING_HISTORY_DELETE_DELTA = "filing history delete delta json string";
    @InjectMocks
    private FilingHistoryDeltaDeserialiser deserialiser;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FilingHistoryDelta expectedDelta;
    @Mock
    private FilingHistoryDeleteDelta expectedDeleteDelta;

    @Test
    void shouldDeserialiseFilingHistoryDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(FilingHistoryDelta.class))).thenReturn(expectedDelta);

        // when
        FilingHistoryDelta actual = deserialiser.deserialiseFilingHistoryDelta(FILING_HISTORY_DELTA);

        // then
        assertEquals(expectedDelta, actual);
        verify(objectMapper).readValue(FILING_HISTORY_DELTA, FilingHistoryDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrown() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(FilingHistoryDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialiseFilingHistoryDelta(FILING_HISTORY_DELTA);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise delta", actual.getMessage());
        verify(objectMapper).readValue(FILING_HISTORY_DELTA, FilingHistoryDelta.class);
    }

    @Test
    void shouldDeserialiseFilingHistoryDeleteDelta() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(FilingHistoryDeleteDelta.class))).thenReturn(expectedDeleteDelta);

        // when
        FilingHistoryDeleteDelta actual = deserialiser.deserialiseFilingHistoryDeleteDelta(FILING_HISTORY_DELETE_DELTA);

        // then
        assertEquals(expectedDeleteDelta, actual);
        verify(objectMapper).readValue(FILING_HISTORY_DELETE_DELTA, FilingHistoryDeleteDelta.class);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenJsonProcessingExceptionThrownFromDeleteDelta()
            throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(FilingHistoryDeleteDelta.class))).thenThrow(
                JsonProcessingException.class);

        // when
        Executable executable = () -> deserialiser.deserialiseFilingHistoryDeleteDelta(FILING_HISTORY_DELETE_DELTA);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise DELETE delta", actual.getMessage());
        verify(objectMapper).readValue(FILING_HISTORY_DELETE_DELTA, FilingHistoryDeleteDelta.class);
    }
}