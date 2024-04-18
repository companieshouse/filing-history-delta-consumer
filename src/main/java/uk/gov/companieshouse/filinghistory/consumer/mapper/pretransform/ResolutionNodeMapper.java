package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class ResolutionNodeMapper implements ChildNodeMapper {

    private static final String CHILD_ARRAY_KEY = "resolutions";

    private final ObjectMapper objectMapper;
    private final FormatDate formatDate;

    public ResolutionNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        this.objectMapper = objectMapper;
        this.formatDate = formatDate;
    }

    @Override
    public ChildPair mapChildObjectNode(FilingHistory delta) {
        ObjectNode objectNode = objectMapper.createObjectNode()
                .put("type", delta.getFormType())
                .put("date", formatDate.format(delta.getReceiveDate()))
                .put("description",
                        StringUtils.isNotBlank(delta.getDescription()) ? delta.getDescription() : "");

        DescriptionValues descriptionValues = delta.getDescriptionValues();

        objectNode
                .putObject("description_values")
                .put("case_start_date", descriptionValues.getCaseStartDate())
                .put("res_type", descriptionValues.getResType())
                .put("description", descriptionValues.getDescription())
                .put("date", descriptionValues.getDate())
                .put("resolution_date", descriptionValues.getResolutionDate());

        return new ChildPair(CHILD_ARRAY_KEY, objectNode);
    }
}
