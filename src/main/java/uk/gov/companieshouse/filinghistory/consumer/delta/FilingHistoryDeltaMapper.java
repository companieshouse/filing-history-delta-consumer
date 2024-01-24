package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerService;

@Component
public class FilingHistoryDeltaMapper {

    private final TransactionKindService kindService;

    private final TransformerService transformerService;

    public FilingHistoryDeltaMapper(TransactionKindService kindService, TransformerService transformerService) {
        this.kindService = kindService;
        this.transformerService = transformerService;
    }

    public InternalFilingHistoryApi map(FilingHistoryDelta delta) {
        TransactionKindCriteria criteria = null;
        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);

        InternalFilingHistoryApi apiRequest = new InternalFilingHistoryApi();


        // TODO map fields pre transform, new dependency
        // TODO convert POJO to JsonNode, new dependency if complex
        // TODO perform transform, transformerService
        // TODO convert JsonNode to apiRequest or new POJO if needed, same dependency as above again
        // TODO clean up fields, mostly child transactions
        // TODO sort/order fields? not needed (probably)
        return apiRequest;
    }
}
