package uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.Transformer;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.ExecArgs;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.SetterArgs;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Then;

public record ThenProperties(@JsonProperty("define") Map<String, String> define,
                             @JsonProperty("set") Map<String, Object> set,
                             @JsonProperty("exec") Map<String, List<String>> exec) {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("^\\[%\\s([\\w.]+)\\s\\|\\s(\\w+)\\s%]$");
    private static final Pattern PLACEHOLDER_NO_FUNCTION_PATTERN = Pattern.compile("^\\[%\\s(\\w+)\\s%]$");
    private static final Pattern EXEC_PLACEHOLDER_PATTERN = Pattern.compile("^\\[%\\s([\\w.]+)\\s%]$");

    public Then compile(TransformerFactory transformerFactory) {

        ExecArgs execArgs;
        if (define != null && exec != null) {
            Pattern extract = define.containsKey("extract") ? Pattern.compile(
                    define.get("extract")) : null;
            String altDescription = define.get("alt_description");
            execArgs = new ExecArgs(transformerFactory.getProcessCapital(),
                    extractFieldPath(exec.get("process_capital").getFirst()), extract,
                    altDescription);
        } else {
            execArgs = null;
        }

        Map<String, SetterArgs> setElements = set.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> buildSetterArgs(e, transformerFactory)));

        return new Then(setElements, execArgs);
    }

    @SuppressWarnings("unchecked")
    private static SetterArgs buildSetterArgs(Entry<String, Object> entry,
            TransformerFactory transformerFactory) {
        SetterArgs setterArgs;
        if (entry.getValue() instanceof List) {
            setterArgs = new SetterArgs(transformerFactory.getReplaceProperty(), (List<String>) entry.getValue());
        } else {
            String value = entry.getValue().toString();
            Matcher placeholderMatcher = PLACEHOLDER_PATTERN.matcher(value);
            Matcher placeholderNoFunctionMatcher = PLACEHOLDER_NO_FUNCTION_PATTERN.matcher(value);
            if (placeholderMatcher.matches()) {
                String sourcePath = placeholderMatcher.group(1);
                Transformer transformer = transformerFactory.mapTransformer(placeholderMatcher.group(2));
                setterArgs = new SetterArgs(transformer, List.of(sourcePath));
            } else if (placeholderNoFunctionMatcher.matches()) {
                String sourcePath = placeholderNoFunctionMatcher.group(1);
                setterArgs = new SetterArgs(transformerFactory.getReplaceProperty(), List.of(sourcePath));
            } else {
                setterArgs = new SetterArgs(transformerFactory.getReplaceProperty(), List.of(value));
            }
        }
        return setterArgs;
    }

    private static String extractFieldPath(String rawFieldPath) {
        Matcher matcher = EXEC_PLACEHOLDER_PATTERN.matcher(rawFieldPath);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid field path in exec %s".formatted(rawFieldPath));
    }
}
