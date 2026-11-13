package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class AnnotationNodeMapper extends AbstractNodeMapper implements ChildNodeMapper {

    protected AnnotationNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        super(objectMapper, formatDate);
    }

    @Override
    public ObjectNode mapChildObjectNode(FilingHistory filingHistory, ObjectNode parentNode) {
        ObjectNode childNode = objectMapper.createObjectNode()
                .put("type", filingHistory.getFormType())
                .put("date", formatDate.format(filingHistory.getReceiveDate()))
                .put("annotation",
                        StringUtils.isNotBlank(filingHistory.getDescription()) ? filingHistory.getDescription() : "");

        ObjectNode dataNode = (ObjectNode) parentNode.get("data");
        dataNode.putArray("annotations").add(childNode);
        return parentNode;
    }
}
