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
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Then;

public record ThenProperties(@JsonProperty("define") Map<String, String> define,
                             @JsonProperty("set") Map<String, Object> set,
                             @JsonProperty("exec") Map<String, List<String>> exec) {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^\\[%\\s([\\w.]*)\\s\\|\\s([\\w_]*)\\s%]$");

    public Then compile(TransformerFactory transformerFactory) {
        // Parse the entries in the set, define and exec to:

        //  - define: ?? Apply regex to some field. Check Perl ??
        Map<String, Pattern> defineElements = define != null ? define.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> Pattern.compile(e.getValue())
                )) : Map.of();

        Map<String, SetterArgs> setElements = set.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> buildSetterArgs(e, transformerFactory)));

        //  - exec: ?? field name -> Call a custom method to build the value. Check Perl ??

        return new Then(defineElements, setElements, Map.of());
    }

    @SuppressWarnings("unchecked")
    private static SetterArgs buildSetterArgs(Entry<String, Object> entry,
            TransformerFactory transformerFactory) {
        SetterArgs setterArgs;
        if (entry.getValue() instanceof List) {
            setterArgs = new SetterArgs(new ReplaceProperty(), (List<String>) entry.getValue());
        } else {
            String value = entry.getValue().toString();
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
            if (matcher.matches()) {
                String sourcePath = matcher.group(1);
                Transformer transformer = transformerFactory.mapTransformer(matcher.group(2));
                setterArgs = new SetterArgs(transformer, List.of(sourcePath));
            } else {
                setterArgs = new SetterArgs(new ReplaceProperty(), List.of(value));
            }
        }
        return setterArgs;
    }
}
