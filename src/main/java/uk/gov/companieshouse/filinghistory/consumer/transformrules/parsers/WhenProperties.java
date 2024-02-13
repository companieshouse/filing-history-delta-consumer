package uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.When;

public record WhenProperties(@JsonProperty("eq") Map<String, String> eq,
                             @JsonProperty("like") Map<String, String> like) {

    public When compile() {
        Objects.requireNonNull(eq, "Missing eq field");

        Entry<String, String> eqEntry = eq.entrySet().stream()
                .filter(e -> (e.getKey().equals("data.type") || e.getKey().equals("type"))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing data.type or type data"));

        Map<String, Pattern> likeElements = like != null ? like.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> Pattern.compile(e.getValue()))
                ) : Map.of();

        return new When(eqEntry.getKey(), eqEntry.getValue(), likeElements);
    }
}

