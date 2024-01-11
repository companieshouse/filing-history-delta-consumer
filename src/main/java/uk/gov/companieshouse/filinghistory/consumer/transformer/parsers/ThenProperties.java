package uk.gov.companieshouse.filinghistory.consumer.transformer.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.transformer.FormatProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformer.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformer.SetterArgs;
import uk.gov.companieshouse.filinghistory.consumer.transformer.Then;

public record ThenProperties(@JsonProperty("define") Map<String, String> define,
                             @JsonProperty("set") Map<String, Object> set,
                             @JsonProperty("exec") Map<String, List<String>> exec) {

    public Then compile() {
        // Parse the entries in the set, define and exec to:

        //  - define: ?? Apply regex to some field. Check Perl ??
        Map<String, Pattern> defineElements = define != null ? define.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> Pattern.compile(e.getValue()))) : Map.of();

        Map<String, SetterArgs> setElements = set.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, ThenProperties::buildSetterArgs));

        //  - exec: ?? field name -> Call a custom method to build the value. Check Perl ??

        return new Then(defineElements, setElements, Map.of());
    }

    @SuppressWarnings("unchecked")
    private static SetterArgs buildSetterArgs(Entry<String, Object> entry) {
        SetterArgs setterArgs;
        if (entry.getValue() instanceof List) {
            setterArgs = new SetterArgs(new ReplaceProperty(), (List<String>) entry.getValue());
        } else {
            String value = entry.getValue().toString();
            if (value.startsWith("'[%")) {
                setterArgs = new SetterArgs(new FormatProperty(), List.of(value));
            } else {
                setterArgs = new SetterArgs(new ReplaceProperty(), List.of(value));
            }
        }
        return setterArgs;
    }


}
