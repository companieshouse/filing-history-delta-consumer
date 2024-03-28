package uk.gov.companieshouse.filinghistory.consumer.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
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
        final FilingHistory filingHistory = delta.getFilingHistory().getFirst();

        TransactionKindResult kindResult = kindService
                .encodeIdByTransactionKind(buildTransactionCriteria(filingHistory));
        ObjectNode topLevelObjectNode = preTransformMapper.mapDeltaToObjectNode(filingHistory);

        // Transform top level
        ObjectNode transformedJsonNode = (ObjectNode) transformerService.transform(topLevelObjectNode);

        // If FH is child
        if (!InternalData.TransactionKindEnum.TOP_LEVEL.equals(kindResult.kind())) {

            Map<String, ObjectNode> objectMap = preTransformMapper
                    .mapChildDeltaToObjectNode(kindResult.kind(), filingHistory);

            ObjectNode dataNode = (ObjectNode) transformedJsonNode.get("data");

            // TODO: Category currently not being mapped correctly
            objectMap.forEach((key, value) -> dataNode.putArray(key)
                    .add(transformerService.transform(value)));
        }

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                transformedJsonNode,
                kindResult,
                delta.getFilingHistory().getFirst().getCompanyNumber(),
                delta.getDeltaAt(),
                updatedBy);

        return internalFilingHistoryApiMapper.mapJsonNodeToInternalFilingHistoryApi(arguments);
    }

    private static TransactionKindCriteria buildTransactionCriteria(final FilingHistory filingHistory) {
        return new TransactionKindCriteria(
                filingHistory.getEntityId(), filingHistory.getParentEntityId(), filingHistory.getFormType(),
                filingHistory.getParentFormType(), filingHistory.getBarcode());
    }
}
