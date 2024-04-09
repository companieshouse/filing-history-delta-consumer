package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.ChildProperties;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class EmbeddedChildNodeMapper implements ChildNodeMapper {

    private static final String CHILD_ARRAY_KEY = "associated_filings";

    private final ObjectMapper objectMapper;
    private final FormatDate formatDate;

    public EmbeddedChildNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        this.objectMapper = objectMapper;
        this.formatDate = formatDate;
    }

    @Override
    public ChildPair mapChildObjectNode(FilingHistory delta) {

        ChildProperties embeddedChild = delta.getChild().getFirst();

        ObjectNode objectNode = objectMapper.createObjectNode()
                .put("type", embeddedChild.getFormType())
                .put("date", formatDate.format(embeddedChild.getReceiveDate()))
                .put("description",
                        StringUtils.isNotBlank(embeddedChild.getDescription()) ? embeddedChild.getDescription() : "");

        return new ChildPair(CHILD_ARRAY_KEY, objectNode);
    }
}
