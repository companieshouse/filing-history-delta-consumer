package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LowerCase {

    String transformLowerCase(String nodeText) {
        if (StringUtils.isBlank(nodeText)) {
            return nodeText;
        }
        return nodeText.toLowerCase();
    }
}
