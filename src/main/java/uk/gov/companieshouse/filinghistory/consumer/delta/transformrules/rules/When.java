package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record When(String field, String formType, Map<String, Pattern> like) {

    public Result match(JsonNode putRequest) {
        String deltaFormType = putRequest.at("/" + field.replace(".", "/")).textValue();

        if (formType.equals(deltaFormType)) {
            Map<String, String> captureGroups = new HashMap<>();
            boolean matched = like.entrySet().stream()
                    .allMatch(e -> {
                        JsonNode deltaLikeField = putRequest.at("/" + e.getKey().replace(".", "/"));
                        if (deltaLikeField != null && deltaLikeField.textValue() != null) {
                            Matcher matcher = e.getValue().matcher(deltaLikeField.textValue());
                            if (matcher.find()) {
                                captureGroups.putAll(matcher.namedGroups().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Entry::getKey,
                                                entry -> matcher.group(entry.getValue()))));
                                return true;
                            }
                        }
                        return false;
                    });
            return new Result(matched, captureGroups);
        }
        return new Result(false, Map.of());
    }
}

