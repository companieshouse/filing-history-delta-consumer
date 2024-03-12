package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
    @Mock
    private DeleteDeltaService deleteDeltaService;

    @Test
    void process() {
        // given
        ChsDelta delta = new ChsDelta();

        // when
        router.route(delta);

        // then
        verify(upsertDeltaService).process(delta);
        verifyNoInteractions(deleteDeltaService);
    }

    @Test
    void processDeleteDelta() {
        // given
        ChsDelta delta = new ChsDelta();
        delta.setIsDelete(true);

        // when
        router.route(delta);

        // then
        verify(deleteDeltaService).process(delta);
        verifyNoInteractions(upsertDeltaService);
    }
}