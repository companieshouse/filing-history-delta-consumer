package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
                        StringUtils.isNotBlank(filingHistory.getDescription()) ? filingHistory.getDescription() : "");

        ObjectNode dataNode = (ObjectNode) parentNode.get("data");
        dataNode.putArray("associated_filings").add(childNode);
        return parentNode;
    }
}
