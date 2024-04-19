package uk.gov.companieshouse.filinghistory.consumer.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.InternalFilingHistoryApiMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.InternalFilingHistoryApiMapperArguments;
import uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform.PreTransformMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerService;

@Component
public class FilingHistoryDeltaProcessor {

    private static final List<String> childArrayKeys = List.of("annotations", "resolutions", "associated_filings");
    private final TransactionKindService kindService;
    private final PreTransformMapper preTransformMapper;
    private final TransformerService transformerService;
    private final InternalFilingHistoryApiMapper internalFilingHistoryApiMapper;

    public FilingHistoryDeltaProcessor(TransactionKindService kindService,
            PreTransformMapper preTransformMapper,
            TransformerService transformerService,
            InternalFilingHistoryApiMapper internalFilingHistoryApiMapper) {
        this.kindService = kindService;
        this.preTransformMapper = preTransformMapper;
        this.transformerService = transformerService;
        this.internalFilingHistoryApiMapper = internalFilingHistoryApiMapper;
    }

    public InternalFilingHistoryApi processDelta(FilingHistoryDelta delta, final String updatedBy) {
        FilingHistory filingHistory = delta.getFilingHistory().getFirst();
        final String entityId = filingHistory.getEntityId();

        TransactionKindCriteria criteria = new TransactionKindCriteria(
                entityId,
                filingHistory.getParentEntityId(),
                filingHistory.getFormType(),
                filingHistory.getParentFormType(),
                filingHistory.getBarcode());

        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);

        ObjectNode preTransformNode = preTransformMapper.mapDeltaToObjectNode(kindResult.kind(), filingHistory);

        ObjectNode transformedParentNode = (ObjectNode) transformerService.transform(preTransformNode, entityId);
        ObjectNode dataNode = (ObjectNode) transformedParentNode.get("data");

        childArrayKeys.forEach(key -> {
            ArrayNode childArray = (ArrayNode) dataNode.get(key);
            if (childArray != null && childArray.get(0) != null) {
                dataNode.putArray(key).add(transformerService.transform(childArray.get(0), entityId));
            }
        });

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                transformedParentNode,
                kindResult,
                delta.getFilingHistory().getFirst().getCompanyNumber(),
                delta.getDeltaAt(),
                updatedBy);

        return internalFilingHistoryApiMapper.mapInternalFilingHistoryApi(arguments);
    }
}
