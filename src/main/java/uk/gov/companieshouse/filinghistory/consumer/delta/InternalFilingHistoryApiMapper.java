package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class InternalFilingHistoryApiMapper {

    private final AnnotationsMapper annotationsMapper;
    private final DescriptionValuesMapper descriptionValuesMapper;
    private final LinksMapper linksMapper;
    private final OriginalValuesMapper originalValuesMapper;

    public InternalFilingHistoryApiMapper(AnnotationsMapper annotationsMapper, DescriptionValuesMapper descriptionValuesMapper, LinksMapper linksMapper, OriginalValuesMapper originalValuesMapper) {
        this.annotationsMapper = annotationsMapper;
        this.descriptionValuesMapper = descriptionValuesMapper;
        this.linksMapper = linksMapper;
        this.originalValuesMapper = originalValuesMapper;
    }

    public InternalFilingHistoryApi mapJsonNodeToInternalFilingHistoryApi(final JsonNode topLevelNode, final TransactionKindResult kindResult, final String deltaAt) {
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");

        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(kindResult.encodedId())
                        .barcode(topLevelNode.get("_barcode").textValue())
                        .type(dataNode.get("type").textValue())
                        .date(dataNode.get("date").textValue())
                        .category(CategoryEnum.fromValue(dataNode.get("category").textValue()))
                        .annotations(annotationsMapper.map(topLevelNode))
                        .subcategory(SubcategoryEnum.fromValue(dataNode.get("subcategory").textValue()))
                        .description(dataNode.get("description").textValue())
                        .descriptionValues(descriptionValuesMapper.map(descriptionValuesNode))
//                        .pages(topLevelNode.get("pages").asInt()) // TODO where is pages?
                        .actionDate(dataNode.get("action_date").textValue())
//                        .paperFiled(topLevelNode.get("paper_filed").asBoolean()) // TODO where is paper filed?
                        .links(linksMapper.map(topLevelNode)))
                .internalData(new InternalData()
                        .originalValues(originalValuesMapper.map(originalValuesNode))
                        .originalDescription(topLevelNode.get("original_description").textValue())
                        .companyNumber(descriptionValuesNode.get("company_number").textValue())
                        .parentEntityId(topLevelNode.get("parent_entity_id").textValue())
                        .entityId(topLevelNode.get("_entity_id").textValue())
                        .documentId(topLevelNode.get("_document_id").textValue())
                        .deltaAt(deltaAt)
//                        .updatedBy("updated_by") // TODO where is updated by?
                        .transactionKind(kindResult.kind()));
    }
}