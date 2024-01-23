package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeltaMapperTest {

    @InjectMocks
    private FilingHistoryDeltaMapper mapper;
    @Mock
    private TransactionKindService kindService;

    @Test
    void map() {
        // given
        FilingHistoryDelta delta = new FilingHistoryDelta();

        InternalFilingHistoryApi expected = new InternalFilingHistoryApi();

        // when
        InternalFilingHistoryApi actual = mapper.map(delta);

        // then
        assertEquals(expected, actual);
    }
}