package uk.gov.companieshouse.filinghistory.consumer.delta;

import static uk.gov.companieshouse.filinghistory.consumer.delta.MappingUtils.getValueFromField;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class InternalFilingHistoryApiMapper {

    private static final Pattern BARCODE_REGEX = Pattern.compile("^X");
    private static final Pattern DOCUMENT_ID_REGEX = Pattern.compile("^...X", Pattern.CASE_INSENSITIVE);

    private final DescriptionValuesMapper descriptionValuesMapper;
    private final LinksMapper linksMapper;
    private final OriginalValuesMapper originalValuesMapper;

    public InternalFilingHistoryApiMapper(DescriptionValuesMapper descriptionValuesMapper, LinksMapper linksMapper, OriginalValuesMapper originalValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
        this.linksMapper = linksMapper;
        this.originalValuesMapper = originalValuesMapper;
    }

    public InternalFilingHistoryApi mapJsonNodeToInternalFilingHistoryApi(final JsonNode topLevelNode, final TransactionKindResult kindResult, final String deltaAt, final String updatedBy) {
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");

        final String barcode = getValueFromField(topLevelNode, "_barcode");
        final String documentId = getValueFromField(topLevelNode, "_document_id");

        final String encodedId = kindResult.encodedId();
        final String companyNumber = getValueFromField(topLevelNode, "company_number");

        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(encodedId)
                        .barcode(barcode)
                        .type(getValueFromField(dataNode, "type"))
                        .date(getValueFromField(dataNode, "date"))
                        .category(getEnumFromField(dataNode, "category", CategoryEnum::fromValue))
                        .subcategory(getEnumFromField(dataNode, "subcategory", SubcategoryEnum::fromValue))
                        .description(getValueFromField(dataNode, "description"))
                        .descriptionValues(descriptionValuesMapper.map(descriptionValuesNode))
                        .actionDate(getValueFromField(dataNode, "action_date"))
                        .paperFiled(isPaperFiled(barcode, documentId))
                        .links(linksMapper.map(companyNumber, encodedId)))
                .internalData(new InternalData()
                        .originalValues(originalValuesMapper.map(originalValuesNode))
                        .originalDescription(getValueFromField(topLevelNode, "original_description"))
                        .companyNumber(companyNumber)
                        .parentEntityId(getValueFromField(topLevelNode, "parent_entity_id"))
                        .entityId(getValueFromField(topLevelNode, "_entity_id"))
                        .documentId(documentId)
                        .deltaAt(deltaAt)
                        .updatedBy(updatedBy)
                        .transactionKind(kindResult.kind()));
    }

    private <T extends Enum<?>> T getEnumFromField(final JsonNode node, final String field, Function<String, T> fromValue) {
        return Optional.ofNullable(node.get(field))
                .map(JsonNode::textValue)
                .map(fromValue)
                .orElse(null);
    }

    private boolean isPaperFiled(final String barcode, final String documentId) {
        if (!StringUtils.isBlank(barcode) && !BARCODE_REGEX.matcher(barcode).find()) {
            return true;
        }
        return !StringUtils.isBlank(documentId) && !DOCUMENT_ID_REGEX.matcher(documentId).find();
    }
}