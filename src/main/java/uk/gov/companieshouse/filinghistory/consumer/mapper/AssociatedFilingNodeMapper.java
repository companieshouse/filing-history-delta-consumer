package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class AssociatedFilingNodeMapper implements ChildNodeMapper {

    private final ObjectMapper objectMapper;
    private final FormatDate formatDate;

    public AssociatedFilingNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
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

        return new ChildPair("associated_filings", objectNode);
    }
}
