package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getFieldValueFromJsonNode;
import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getNestedJsonNodeFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.service.TransactionKindResult;

@Component
public class InternalFilingHistoryApiMapper {

    private final SubcategoryMapper subcategoryMapper;
    private final DescriptionValuesMapper descriptionValuesMapper;
    private final LinksMapper linksMapper;
    private final OriginalValuesMapper originalValuesMapper;
    private final PaperFiledMapper paperFiledMapper;

    public InternalFilingHistoryApiMapper(SubcategoryMapper subcategoryMapper,
            DescriptionValuesMapper descriptionValuesMapper, LinksMapper linksMapper,
            OriginalValuesMapper originalValuesMapper, PaperFiledMapper paperFiledMapper) {
        this.subcategoryMapper = subcategoryMapper;
        this.descriptionValuesMapper = descriptionValuesMapper;
        this.linksMapper = linksMapper;
        this.originalValuesMapper = originalValuesMapper;
        this.paperFiledMapper = paperFiledMapper;
    }

    public InternalFilingHistoryApi mapJsonNodeToInternalFilingHistoryApi(
            InternalFilingHistoryApiMapperArguments arguments) {
        final TransactionKindResult kindResult = arguments.kindResult();
        final String encodedId = kindResult.encodedId();
        final String companyNumber = arguments.companyNumber();
        final JsonNode topLevelNode = arguments.topLevelNode();

        String barcode = getFieldValueFromJsonNode(topLevelNode, "_barcode");
        String documentId = getFieldValueFromJsonNode(topLevelNode, "_document_id");

        InternalFilingHistoryApi requestObject = new InternalFilingHistoryApi()
                .externalData(new ExternalData())
                .internalData(new InternalData());

        requestObject.getInternalData()
                .originalDescription(getFieldValueFromJsonNode(topLevelNode, "original_description"))
                .parentEntityId(getFieldValueFromJsonNode(topLevelNode, "parent_entity_id"))
                .entityId(getFieldValueFromJsonNode(topLevelNode, "_entity_id"))
                .originalValues(
                        originalValuesMapper.map(getNestedJsonNodeFromJsonNode(topLevelNode, "original_values")))
                .companyNumber(companyNumber)
                .documentId(documentId)
                .deltaAt(arguments.deltaAt())
                .updatedBy(arguments.updatedBy())
                .transactionKind(kindResult.kind());

        final JsonNode dataNode = getNestedJsonNodeFromJsonNode(topLevelNode, "data");
        requestObject.getExternalData()
                .type(getFieldValueFromJsonNode(dataNode, "type"))
                .date(getFieldValueFromJsonNode(dataNode, "date"))
                // TODO: For the example in annotation_delta.json, the category field is set to "9" which doesn't match any category enum values
                /*
                * The issue here is that the transform rules match on type rather than data.type. In the pre-transform,
                * the type is set to the data.type field. Needs further investigation.
                */
                .category(getEnumFromCategory(dataNode, CategoryEnum::fromValue))
                .subcategory(subcategoryMapper.map(dataNode))
                .description(getFieldValueFromJsonNode(dataNode, "description"))
                .actionDate(getFieldValueFromJsonNode(dataNode, "action_date"))
                .transactionId(encodedId)
                .barcode(barcode)
                .descriptionValues(
                        descriptionValuesMapper.map(getNestedJsonNodeFromJsonNode(dataNode, "description_values")))
                .paperFiled(paperFiledMapper.isPaperFiled(barcode, documentId) ? true : null)
                .links(linksMapper.map(companyNumber, encodedId));

        return requestObject;
    }

    private static <T extends Enum<?>> T getEnumFromCategory(final JsonNode node, Function<String, T> fromValue) {
        return Optional.ofNullable(node)
                .map(n -> n.get("category"))
                .map(JsonNode::textValue)
                .map(fromValue)
                .orElse(null);
    }
}