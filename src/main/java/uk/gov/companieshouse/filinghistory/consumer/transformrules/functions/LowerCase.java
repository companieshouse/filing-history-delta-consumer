package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import org.apache.commons.lang3.StringUtils;
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
