package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;

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

    @Test
    void processDeleteDelta() {
        // given
        ChsDelta delta = new ChsDelta();
        delta.setIsDelete(true);

        // when
        Executable executable = () -> router.route(delta);

        // then
        assertThrows(NonRetryableException.class, executable);
        verifyNoInteractions(upsertDeltaService);
    }
}