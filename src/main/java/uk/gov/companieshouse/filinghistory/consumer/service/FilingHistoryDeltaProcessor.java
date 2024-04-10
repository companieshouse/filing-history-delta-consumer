package uk.gov.companieshouse.filinghistory.consumer.service;

import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.mapper.ChildPair;
import uk.gov.companieshouse.filinghistory.consumer.mapper.InternalFilingHistoryApiMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.InternalFilingHistoryApiMapperArguments;
import uk.gov.companieshouse.filinghistory.consumer.mapper.PreTransformMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerService;

@Component
public class FilingHistoryDeltaProcessor {

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
                filingHistory.getEntityId(),
                filingHistory.getParentEntityId(),
                filingHistory.getFormType(),
                filingHistory.getParentFormType(),
                filingHistory.getBarcode());

        TransactionKindResult kindResult = kindService.encodeIdByTransactionKind(criteria);

        ObjectNode topLevelObjectNode = preTransformMapper.mapDeltaToObjectNode(filingHistory);
        ObjectNode transformedJsonNode = (ObjectNode) transformerService.transform(topLevelObjectNode, entityId);

        if (!TOP_LEVEL.equals(kindResult.kind())) {
            ChildPair childPair = preTransformMapper.mapChildDeltaToObjectNode(kindResult.kind(), filingHistory);
            ObjectNode dataNode = (ObjectNode) transformedJsonNode.get("data");
            dataNode.putArray(childPair.type()).add(transformerService.transform(childPair.node(), entityId));
        }

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                transformedJsonNode,
                kindResult,
                delta.getFilingHistory().getFirst().getCompanyNumber(),
                delta.getDeltaAt(),
                updatedBy);

        return internalFilingHistoryApiMapper.mapInternalFilingHistoryApi(arguments);
    }
}
