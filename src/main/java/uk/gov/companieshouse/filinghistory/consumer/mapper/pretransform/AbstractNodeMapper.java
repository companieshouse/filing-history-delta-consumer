package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

public abstract class AbstractNodeMapper {

    protected final ObjectMapper objectMapper;
    protected final FormatDate formatDate;

    protected AbstractNodeMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        this.objectMapper = objectMapper;
        this.formatDate = formatDate;
    }

    protected static void putIfNotBlank(ObjectNode node, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            node.put(key, value);
        }
    }
}
