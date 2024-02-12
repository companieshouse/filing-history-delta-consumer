package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FormatNumber {

    public String apply(String numberString) {
        if (StringUtils.isEmpty(numberString)) {
            return numberString;
        }
        String[] integerAndDecimal = numberString.split("\\.");

        String integerString = "%,d".formatted(Integer.parseInt(integerAndDecimal[0]));

        if (integerAndDecimal.length > 1) {
            return "%s.%s".formatted(integerString, integerAndDecimal[1]);
        }
        return integerString;
    }
}
