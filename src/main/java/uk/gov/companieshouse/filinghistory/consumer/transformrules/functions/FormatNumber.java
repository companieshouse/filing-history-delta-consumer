package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FormatNumber {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^([-+]?\\d+)(\\d{3})");

    public String apply(String numberString) {
        if (StringUtils.isEmpty(numberString)) {
            return numberString;
        }
        String formattedNumber = numberString;
        while (NUMBER_PATTERN.matcher(formattedNumber).find()) {
            formattedNumber = formattedNumber.replaceAll(NUMBER_PATTERN.pattern(), "$1,$2");
        }
        return formattedNumber;
    }
}
