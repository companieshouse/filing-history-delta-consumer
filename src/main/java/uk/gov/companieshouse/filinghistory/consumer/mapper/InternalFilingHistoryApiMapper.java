package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getFieldValueFromJsonNode;
import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getNestedJsonNodeFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
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
    private final ChildRequestMapperFactory childRequestMapperFactory;

    public InternalFilingHistoryApiMapper(SubcategoryMapper subcategoryMapper,
                                          DescriptionValuesMapper descriptionValuesMapper, LinksMapper linksMapper,
                                          OriginalValuesMapper originalValuesMapper, PaperFiledMapper paperFiledMapper, ChildRequestMapperFactory childRequestMapperFactory) {
        this.subcategoryMapper = subcategoryMapper;
        this.descriptionValuesMapper = descriptionValuesMapper;
        this.linksMapper = linksMapper;
        this.originalValuesMapper = originalValuesMapper;
        this.paperFiledMapper = paperFiledMapper;
        this.childRequestMapperFactory = childRequestMapperFactory;
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

        /*
            TODO: Map to child array and object
         */
        if (StringUtils.isNotBlank(requestObject.getInternalData().getParentEntityId())) {
            requestObject = childRequestMapperFactory
                    .getChildRequestMapper(dataNode
                            .get("type")
                            .textValue())
                    .map(dataNode);
        }

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