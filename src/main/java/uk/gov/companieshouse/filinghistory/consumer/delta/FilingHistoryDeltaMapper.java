package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class FilingHistoryDeltaMapper {

    private final TransactionKindService kindService;

    public FilingHistoryDeltaMapper(TransactionKindService kindService) {
        this.kindService = kindService;
    }

    public InternalFilingHistoryApi map(FilingHistoryDelta delta) {
        TransactionKindCriteria criteria = null;
        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);

        InternalFilingHistoryApi apiRequest = new InternalFilingHistoryApi();

        // TODO map fields pre transform
        // TODO perform transform
        // TODO clean up fields
        // TODO sort/order fields?
        return apiRequest;
    }
}
