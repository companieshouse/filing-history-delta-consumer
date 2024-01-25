package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerService;

@Component
public class FilingHistoryDeltaMapper {

    private final TransactionKindService kindService;
    private final PreTransformMapper preTransformMapper;
    private final TransformerService transformerService;
    private final InternalFilingHistoryApiMapper internalFilingHistoryApiMapper;
    private final ObjectMapper objectMapper;

    public FilingHistoryDeltaMapper(TransactionKindService kindService, PreTransformMapper preTransformMapper, TransformerService transformerService, InternalFilingHistoryApiMapper internalFilingHistoryApiMapper, ObjectMapper objectMapper) {
        this.kindService = kindService;
        this.preTransformMapper = preTransformMapper;
        this.transformerService = transformerService;
        this.internalFilingHistoryApiMapper = internalFilingHistoryApiMapper;
        this.objectMapper = objectMapper;
    }

    public InternalFilingHistoryApi map(FilingHistoryDelta delta) {
        final FilingHistory filingHistory = delta.getFilingHistory().getFirst();
        TransactionKindCriteria criteria = new TransactionKindCriteria(
                filingHistory.getEntityId(), filingHistory.getParentEntityId(), filingHistory.getFormType(),
                filingHistory.getParentFormType(), filingHistory.getBarcode());
        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);

        final JsonNode transformedJsonNode = transformerService.transform(preTransformMapper.mapDeltaToObjectNode(filingHistory));

        // TODO convert JsonNode to apiRequest or new POJO if needed, same dependency as above again
        // TODO after we have transformed the delta using the YAML we need to verify description mappings
        // TODO clean up fields, mostly child transactions
        // TODO sort/order fields? not needed (probably)

        return internalFilingHistoryApiMapper.mapJsonNodeToInternalFilingHistoryApi(transformedJsonNode, kindResult, delta.getDeltaAt());
    }
}
