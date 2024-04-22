package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.ChildProperties;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class EmbeddedChildNodeMapper extends AbstractNodeMapper implements ChildNodeMapper {

    protected EmbeddedChildNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        super(objectMapper, formatDate);
    }

    @Override
    public ObjectNode mapChildObjectNode(FilingHistory filingHistory, ObjectNode parentNode) {
        ChildProperties embeddedChild = filingHistory.getChild().getFirst();

        ObjectNode childNode = objectMapper.createObjectNode()
                .put("type", embeddedChild.getFormType())
                .put("date", formatDate.format(embeddedChild.getReceiveDate()))
                .put("description",
                        StringUtils.isNotBlank(embeddedChild.getDescription()) ? embeddedChild.getDescription() : "");

        ObjectNode dataNode = (ObjectNode) parentNode.get("data");
        dataNode.putArray("associated_filings").add(childNode);
        return parentNode;
    }
}
