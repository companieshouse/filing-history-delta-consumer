package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class InternalFilingHistoryApiMapper {

    public InternalFilingHistoryApi mapJsonNodeToInternalFilingHistoryApi(final JsonNode jsonNode, final TransactionKindResult kindResult, final String deltaAt) {
        return new InternalFilingHistoryApi()

                .externalData(new ExternalData()
                        .transactionId(kindResult.encodedId())
                        .barcode(jsonNode.get("_barcode").textValue())
                        .type(jsonNode.get("type").textValue())
                        .date(jsonNode.get("date").textValue())
                        .category(categoryEnumMapper(jsonNode.get("category").textValue()))
                        .annotations(List.of(new FilingHistoryItemDataAnnotations()))
                        .subcategory(subcategoryEnumMapper(jsonNode.get("subcategory").textValue()))
                        .description(jsonNode.get("description").textValue())
                        .descriptionValues(new FilingHistoryItemDataDescriptionValues())
                        .pages(jsonNode.get("pages").asInt())
                        .actionDate(jsonNode.get("action_date").textValue())
                        .paperFiled(jsonNode.get("paper_filed").asBoolean())
                        .links(new FilingHistoryItemDataLinks()))

                .internalData(new InternalData()
                        .originalValues(new InternalDataOriginalValues())
                        .originalDescription(jsonNode.get("original_description").textValue())
                        .companyNumber(jsonNode.get("company_number").textValue())
                        .parentEntityId(jsonNode.get("parent_entity_id").textValue())
                        .entityId(jsonNode.get("_entity_id").textValue())
                        .documentId(jsonNode.get("_document_id").textValue())
                        .deltaAt(deltaAt)
                        .updatedBy("updated_by")
                        .transactionKind(kindResult.kind()));

        // TODO need mappers for nested objects
    }

    private CategoryEnum categoryEnumMapper(String category) {
        return CategoryEnum.fromValue(category);
    }

    private SubcategoryEnum subcategoryEnumMapper(String subcategory) {
        return SubcategoryEnum.fromValue(subcategory);
    }
}