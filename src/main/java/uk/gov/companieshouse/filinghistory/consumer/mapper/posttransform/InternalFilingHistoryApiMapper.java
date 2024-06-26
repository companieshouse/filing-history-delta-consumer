package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;
import static uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.MapperUtils.getFieldValueFromJsonNode;
import static uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.MapperUtils.getNestedJsonNodeFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.consumer.service.TransactionKindResult;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class InternalFilingHistoryApiMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final OriginalValuesMapper originalValuesMapper;
    private final ExternalDataMapper externalDataMapper;

    public InternalFilingHistoryApiMapper(OriginalValuesMapper originalValuesMapper,
                                          ExternalDataMapper externalDataMapper) {
        this.originalValuesMapper = originalValuesMapper;
        this.externalDataMapper = externalDataMapper;
    }

    public InternalFilingHistoryApi mapInternalFilingHistoryApi(InternalFilingHistoryApiMapperArguments args) {
        JsonNode topLevelNode = args.topLevelNode();

        TransactionKindResult kindResult = Optional.ofNullable(args.kindResult())
                .orElseGet(() -> {
                    LOGGER.error("Null transaction kind result provided", DataMapHolder.getLogMap());
                    throw new NonRetryableException("Null transaction kind result provided");
                });

        String barcode = getFieldValueFromJsonNode(topLevelNode, "_barcode");
        String matchedDefault = getFieldValueFromJsonNode(topLevelNode, "matched_default");

        InternalData internalData = new InternalData()
                .originalDescription(getFieldValueFromJsonNode(topLevelNode, "original_description"))
                .parentEntityId(getFieldValueFromJsonNode(topLevelNode, "parent_entity_id"))
                .entityId(getFieldValueFromJsonNode(topLevelNode, "_entity_id"))
                .matchedDefault(matchedDefault == null ? null : Integer.parseInt(matchedDefault))
                .originalValues(
                        originalValuesMapper.map(getNestedJsonNodeFromJsonNode(topLevelNode, "original_values")))
                .companyNumber(args.companyNumber())
                .documentId(getFieldValueFromJsonNode(topLevelNode, "_document_id"))
                .deltaAt(args.deltaAt())
                .updatedBy(args.updatedBy())
                .transactionKind(kindResult.kind());

        ExternalData externalData = externalDataMapper.mapExternalData(topLevelNode, barcode,
                kindResult.encodedId(), args.companyNumber());

        return new InternalFilingHistoryApi()
                .internalData(internalData)
                .externalData(externalData);
    }
}