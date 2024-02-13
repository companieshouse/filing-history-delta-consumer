package uk.gov.companieshouse.filinghistory.consumer.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
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

    public FilingHistoryDeltaProcessor(TransactionKindService kindService, PreTransformMapper preTransformMapper, TransformerService transformerService, InternalFilingHistoryApiMapper internalFilingHistoryApiMapper) {
        this.kindService = kindService;
        this.preTransformMapper = preTransformMapper;
        this.transformerService = transformerService;
        this.internalFilingHistoryApiMapper = internalFilingHistoryApiMapper;
    }

    public InternalFilingHistoryApi processDelta(FilingHistoryDelta delta, final String updatedBy) {
        final FilingHistory filingHistory = delta.getFilingHistory().getFirst();
        final JsonNode transformedJsonNode = transformerService.transform(
                preTransformMapper.mapDeltaToObjectNode(filingHistory));
        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                transformedJsonNode,
                kindService.encodeIdByTransactionKind(buildTransactionCriteria(filingHistory)),
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
