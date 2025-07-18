package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.MapperUtils.getFieldValueFromJsonNode;
import static uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.MapperUtils.getNestedJsonNodeFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.Resolution;
import uk.gov.companieshouse.filinghistory.consumer.serdes.ArrayNodeDeserialiser;

@Component
public class ExternalDataMapper {

    private final SubcategoryMapper subcategoryMapper;
    private final DescriptionValuesMapper descriptionValuesMapper;
    private final PaperFiledMapper paperFiledMapper;
    private final LinksMapper linksMapper;
    private final ArrayNodeDeserialiser<Annotation> annotationsDeserialiser;
    private final ArrayNodeDeserialiser<Resolution> resolutionsDeserialiser;
    private final ArrayNodeDeserialiser<AssociatedFiling> associatedFilingDeserialiser;

    public ExternalDataMapper(SubcategoryMapper subcategoryMapper,
            DescriptionValuesMapper descriptionValuesMapper, PaperFiledMapper paperFiledMapper,
            LinksMapper linksMapper, ArrayNodeDeserialiser<Annotation> annotationsDeserialiser,
            ArrayNodeDeserialiser<Resolution> resolutionsDeserialiser,
            ArrayNodeDeserialiser<AssociatedFiling> associatedFilingDeserialiser) {
        this.subcategoryMapper = subcategoryMapper;
        this.descriptionValuesMapper = descriptionValuesMapper;
        this.paperFiledMapper = paperFiledMapper;
        this.linksMapper = linksMapper;
        this.annotationsDeserialiser = annotationsDeserialiser;
        this.resolutionsDeserialiser = resolutionsDeserialiser;
        this.associatedFilingDeserialiser = associatedFilingDeserialiser;
    }

    ExternalData mapExternalData(JsonNode topLevelNode, String barcode, String encodedId, String companyNumber) {
        JsonNode dataNode = getNestedJsonNodeFromJsonNode(topLevelNode, "data");
        String category = getFieldValueFromJsonNode(dataNode, "category");
        String formType = getFieldValueFromJsonNode(dataNode, "form_type");

        List<Annotation> annotations = Optional.ofNullable(
                        getNestedJsonNodeFromJsonNode(dataNode, "annotations"))
                .map(annotationsArray -> annotationsDeserialiser.deserialise((ArrayNode) annotationsArray))
                .orElse(null);

        List<Resolution> resolutions = Optional.ofNullable(
                        getNestedJsonNodeFromJsonNode(dataNode, "resolutions"))
                .map(resolutionsArray -> resolutionsDeserialiser.deserialise((ArrayNode) resolutionsArray))
                .orElse(null);

        List<AssociatedFiling> associatedFilings = Optional.ofNullable(
                        getNestedJsonNodeFromJsonNode(dataNode, "associated_filings"))
                .map(afArray -> associatedFilingDeserialiser.deserialise((ArrayNode) afArray))
                .orElse(null);

        return new ExternalData()
                .type(getFieldValueFromJsonNode(dataNode, "type"))
                .date(getFieldValueFromJsonNode(dataNode, "date"))
                .category(category != null ? CategoryEnum.fromValue(category) : null)
                .subcategory(subcategoryMapper.map(dataNode))
                .description(getFieldValueFromJsonNode(dataNode, "description"))
                .actionDate(getFieldValueFromJsonNode(dataNode, "action_date"))
                .transactionId(encodedId)
                .barcode(barcode)
                .descriptionValues(
                        descriptionValuesMapper.map(getNestedJsonNodeFromJsonNode(dataNode, "description_values")))
                .paperFiled(paperFiledMapper.isPaperFiled(barcode, formType) ? true : null)
                .links(linksMapper.map(companyNumber, encodedId))
                .annotations(annotations)
                .resolutions(resolutions)
                .associatedFilings(associatedFilings);
    }
}
