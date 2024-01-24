package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.Transformer;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Default;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

record DefaultProperties(@JsonProperty("set") Map<String, String> set) {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "^\\[%\\s([\\w.]*)\\s\\|\\s([\\w_]*)\\s%]$");

    public Default compile(TransformerFactory transformerFactory) {
        Map<String, SetterArgs> setElements = set.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> buildSetterArgs(e, transformerFactory)));
        return new Default(setElements);
    }

    private static SetterArgs buildSetterArgs(Entry<String, String> entry,
            TransformerFactory transformerFactory) {
        SetterArgs setterArgs;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(entry.getValue());
        if (matcher.matches()) {
            String sourcePath = matcher.group(1);
            Transformer transformer = transformerFactory.mapTransformer(matcher.group(2));
            setterArgs = new SetterArgs(transformer, List.of(sourcePath));
        } else {
            setterArgs = new SetterArgs(new ReplaceProperty(), List.of(entry.getValue()));
        }
        return setterArgs;
    }

}
