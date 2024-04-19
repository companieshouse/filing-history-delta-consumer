package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class ResolutionNodeMapper extends AbstractNodeMapper implements ChildNodeMapper {

    private static final String DESCRIPTION_FIELD = "description";

    protected ResolutionNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        super(objectMapper, formatDate);
    }

    @Override
    public ObjectNode mapChildObjectNode(FilingHistory filingHistory, ObjectNode parentNode) {
        ObjectNode childNode = objectMapper.createObjectNode()
                .put("type", filingHistory.getFormType())
                .put(DESCRIPTION_FIELD,
                        StringUtils.isNotBlank(filingHistory.getDescription()) ? filingHistory.getDescription() : "");

        DescriptionValues descriptionValues = filingHistory.getDescriptionValues();

        if (descriptionValues != null) {
            ObjectNode valuesNode = childNode.putObject("description_values");

            putIfNotBlank(valuesNode, "case_start_date", descriptionValues.getCaseStartDate());
            putIfNotBlank(valuesNode, "res_type", descriptionValues.getResType());
            putIfNotBlank(valuesNode, DESCRIPTION_FIELD, descriptionValues.getDescription());
            putIfNotBlank(valuesNode, "date", descriptionValues.getDate());
            putIfNotBlank(valuesNode, "resolution_date", descriptionValues.getResolutionDate());
        }

        parentNode.remove("_document_id");
        ObjectNode dataNode = (ObjectNode) parentNode.get("data");
        dataNode.put("type", "RESOLUTIONS")
                .put(DESCRIPTION_FIELD, "RESOLUTIONS")
                .putArray("resolutions").add(childNode);
        return parentNode;
    }
}