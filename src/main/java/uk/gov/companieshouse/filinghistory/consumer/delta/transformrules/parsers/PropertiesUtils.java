package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.CaseUtils;

public class PropertiesUtils {

    private static final Pattern GROUP_PATTERN = Pattern.compile("<([^>]*)>");

    private PropertiesUtils() {
    }

    static String convertToCamelCase(String original) {
        Matcher matcher = GROUP_PATTERN.matcher(original);
        String value = original;
        while (matcher.find()) {
            String camelCase = CaseUtils.toCamelCase(matcher.group(1), false, '_');
            value = value.replace(matcher.group(1), camelCase);
        }
        return value;
    }
}
