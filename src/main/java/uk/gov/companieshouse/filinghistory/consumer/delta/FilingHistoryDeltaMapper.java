package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerService;

@Component
public class FilingHistoryDeltaMapper {

    private final TransactionKindService kindService;
    private final PreTransformMapper preTransformMapper;
    private final TransformerService transformerService;
    private final ObjectMapper objectMapper;

    public FilingHistoryDeltaMapper(TransactionKindService kindService, PreTransformMapper preTransformMapper, TransformerService transformerService, ObjectMapper objectMapper) {
        this.kindService = kindService;
        this.preTransformMapper = preTransformMapper;
        this.transformerService = transformerService;
        this.objectMapper = objectMapper;
    }

    public InternalFilingHistoryApi map(FilingHistoryDelta delta) {
        TransactionKindCriteria criteria = null;
        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);
        InternalFilingHistoryApi apiRequest = new InternalFilingHistoryApi();

        final JsonNode oldJsonNode = objectMapper.valueToTree(preTransformMapper.map(delta));

        // TODO perform transform, transformerService
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        Map<String, Object> requestMap = objectMapper.convertValue(transformerService.transform(oldJsonNode), typeRef);

        // TODO convert JsonNode to apiRequest or new POJO if needed, same dependency as above again
        // TODO clean up fields, mostly child transactions
        // TODO sort/order fields? not needed (probably)

        return apiRequest;
    }
}
