package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class AnnotationNodeMapper implements ChildNodeMapper {

    private static final String CHILD_ARRAY_KEY = "annotations";

    private final ObjectMapper objectMapper;
    private final FormatDate formatDate;

    public AnnotationNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        this.objectMapper = objectMapper;
        this.formatDate = formatDate;
    }

    @Override
    public ChildPair mapChildObjectNode(FilingHistory delta) {
        ObjectNode objectNode = objectMapper.createObjectNode()
                .put("type", delta.getFormType())
                .put("date", formatDate.format(delta.getReceiveDate()))
                .put("annotation", mapAnnotationField(delta));

        return new ChildPair(CHILD_ARRAY_KEY, objectNode);
    }

    private String mapAnnotationField(FilingHistory delta) {
        String annotationFieldValue = "";
        if (StringUtils.isNotBlank(delta.getDescription())) {
            annotationFieldValue = delta.getDescription();
        }
        return annotationFieldValue;
    }
}
