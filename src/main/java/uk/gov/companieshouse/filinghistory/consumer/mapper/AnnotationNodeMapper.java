package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class AnnotationNodeMapper implements ChildNodeMapper {

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

        return new ChildPair("annotations", objectNode);
    }

    private String mapAnnotationField(FilingHistory delta) {
        // TODO: Consider making this into a dependency so more easily testable

        String annotationFieldValue = "";

        if (StringUtils.isNotBlank(delta.getDescription())) {
            annotationFieldValue = delta.getDescription();
        }
        // TODO: IF delta.note exists THEN annotationFieldValue = delta.note
        // TODO: Should the note field be on top-level delta or in child properties object in child array?

        return annotationFieldValue;
    }
}
