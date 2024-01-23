package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;

@ExtendWith(MockitoExtension.class)
class DeltaServiceRouterTest {

    @InjectMocks
    private DeltaServiceRouter router;
    @Mock
    private UpsertDeltaService upsertDeltaService;

    @Test
    void process() {
        // given
        ChsDelta delta = new ChsDelta();

        // when
        router.route(delta);

        // then
        verify(upsertDeltaService).process(delta);
    }
}