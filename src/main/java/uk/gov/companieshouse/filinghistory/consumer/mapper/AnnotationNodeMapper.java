package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;

@Component
public class AnnotationNodeMapper implements ChildNodeMapper {

    private final ObjectMapper objectMapper;

    public AnnotationNodeMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, ObjectNode> mapChildObjectNode(FilingHistory delta) {

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode
                .putObject("annotation")
                .put("type", delta.getFormType())
                .put("date", delta.getReceiveDate())
                .put("category", delta.getCategory())
                .put("annotation", mapAnnotationField(delta));

        return Map.of("annotations", objectNode);
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
