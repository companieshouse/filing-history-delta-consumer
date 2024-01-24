package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.AddressCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.BsonDate;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.LowerCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.SentenceCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TitleCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.Transformer;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Then;

public record ThenProperties(@JsonProperty("define") Map<String, String> define,
                             @JsonProperty("set") Map<String, Object> set,
                             @JsonProperty("exec") Map<String, List<String>> exec) {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^\\[%\\s([\\w.]*)\\s\\|\\s([\\w_]*)\\s%]$");

    public Then compile() {
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
                        ThenProperties::buildSetterArgs));

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
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
            if (matcher.matches()) {
                String sourcePath = matcher.group(1);
                Transformer transformer = switch (matcher.group(2)) {
                    case "address_case" -> new AddressCase(); // TODO Make functions Beans and inject
                    case "bson_date" -> new BsonDate();
                    case "lc" -> new LowerCase();
                    case "sentence_case" -> new SentenceCase();
                    case "title_case" -> new TitleCase();
                    default -> throw new IllegalArgumentException("Unexpected function " + matcher.group(2));
                };
                setterArgs = new SetterArgs(transformer, List.of(sourcePath));
            } else {
                setterArgs = new SetterArgs(new ReplaceProperty(), List.of(value));
            }
        }
        return setterArgs;
    }
}
