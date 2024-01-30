package uk.gov.companieshouse.filinghistory.consumer.delta;

import static uk.gov.companieshouse.filinghistory.consumer.delta.MappingUtils.getFieldValueFromJsonNode;
import static uk.gov.companieshouse.filinghistory.consumer.delta.MappingUtils.getNestedJsonNodeFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class InternalFilingHistoryApiMapper {

    private final DescriptionValuesMapper descriptionValuesMapper;
    private final LinksMapper linksMapper;
    private final OriginalValuesMapper originalValuesMapper;
    private final PaperFiledMapper paperFiledMapper;

    public InternalFilingHistoryApiMapper(DescriptionValuesMapper descriptionValuesMapper, LinksMapper linksMapper, OriginalValuesMapper originalValuesMapper, PaperFiledMapper paperFiledMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
        this.linksMapper = linksMapper;
        this.originalValuesMapper = originalValuesMapper;
        this.paperFiledMapper = paperFiledMapper;
    }

    public InternalFilingHistoryApi mapJsonNodeToInternalFilingHistoryApi(InternalFilingHistoryApiMapperArguments arguments) {
        final TransactionKindResult kindResult = arguments.kindResult();
        final String encodedId = kindResult.encodedId();
        final String companyNumber = arguments.companyNumber();

        InternalFilingHistoryApi requestObject = new InternalFilingHistoryApi()
                .externalData(new ExternalData())
                .internalData(new InternalData());

        String barcode = null;
        String documentId = null;

        final JsonNode topLevelNode = arguments.topLevelNode();
        if (topLevelNode != null) {
            mapTopLevelNode(requestObject, topLevelNode);
            barcode = getFieldValueFromJsonNode(topLevelNode, "_barcode");
            documentId = getFieldValueFromJsonNode(topLevelNode, "_document_id");
        }

        final JsonNode dataNode = getNestedJsonNodeFromJsonNode(topLevelNode, "data");
        if (dataNode != null) {
            mapDataNode(requestObject, dataNode);
        }

        requestObject.getExternalData()
                .transactionId(encodedId)
                .barcode(barcode)
                .descriptionValues(descriptionValuesMapper.map(getNestedJsonNodeFromJsonNode(dataNode, "description_values")))
                .paperFiled(paperFiledMapper.map(barcode, documentId))
                .links(linksMapper.map(companyNumber, encodedId));

        requestObject.getInternalData()
                .originalValues(originalValuesMapper.map(getNestedJsonNodeFromJsonNode(topLevelNode, "original_values")))
                .companyNumber(companyNumber)
                .documentId(documentId)
                .deltaAt(arguments.deltaAt())
                .updatedBy(arguments.updatedBy())
                .transactionKind(kindResult.kind());

        return requestObject;
    }

    private static <T extends Enum<?>> T getEnumFromField(final JsonNode node, final String field, Function<String, T> fromValue) {
        return Optional.ofNullable(node.get(field))
                .map(JsonNode::textValue)
                .map(fromValue)
                .orElse(null);
    }

    private static void mapTopLevelNode(InternalFilingHistoryApi requestObject, JsonNode topLevelNode) {
        requestObject
                .getInternalData()
                    .originalDescription(getFieldValueFromJsonNode(topLevelNode, "original_description"))
                    .parentEntityId(getFieldValueFromJsonNode(topLevelNode, "parent_entity_id"))
                    .entityId(getFieldValueFromJsonNode(topLevelNode, "_entity_id"));
    }

    private static void mapDataNode(InternalFilingHistoryApi requestObject, JsonNode dataNode) {
         requestObject
                 .getExternalData()
                    .type(getFieldValueFromJsonNode(dataNode, "type"))
                    .date(getFieldValueFromJsonNode(dataNode, "date"))
                    .category(getEnumFromField(dataNode, "category", CategoryEnum::fromValue))
                    .subcategory(getEnumFromField(dataNode, "subcategory", SubcategoryEnum::fromValue))
                    .description(getFieldValueFromJsonNode(dataNode, "description"))
                    .actionDate(getFieldValueFromJsonNode(dataNode, "action_date"));
    }
}