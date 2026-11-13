package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class AssociatedFilingNodeMapper extends AbstractNodeMapper implements ChildNodeMapper {

    protected AssociatedFilingNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        super(objectMapper, formatDate);
    }

    @Override
    public ObjectNode mapChildObjectNode(FilingHistory filingHistory, ObjectNode parentNode) {
        ObjectNode childNode = objectMapper.createObjectNode()
                .put("type", filingHistory.getFormType())
                .put("date", formatDate.format(filingHistory.getReceiveDate()))
                .put("description",
                        StringUtils.isNotBlank(filingHistory.getDescription()) ?
                                filingHistory.getDescription()
                                        .replace("<", "\\")
                                        .replace("\n", "\\") : "");

        ObjectNode dataNode = (ObjectNode) parentNode.get("data");
        dataNode.putArray("associated_filings").add(childNode);
        return parentNode;
    }
}
