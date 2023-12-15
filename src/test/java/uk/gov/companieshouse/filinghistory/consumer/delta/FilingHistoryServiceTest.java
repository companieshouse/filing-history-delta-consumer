package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.delta.ChsDelta;

class FilingHistoryServiceTest {

    private Service service;

    @BeforeEach
    void setUp() {
        service = new FilingHistoryService();
    }

    @Test
    void process() {
        // given
        ChsDelta delta = new ChsDelta();

        // when
        service.process(delta);

        // then
    }
}