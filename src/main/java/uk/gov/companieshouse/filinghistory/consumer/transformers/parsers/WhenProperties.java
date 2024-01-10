package uk.gov.companieshouse.filinghistory.consumer.transformers.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.transformers.When;

public record WhenProperties(@JsonProperty("eq") Map<String, String> eq,
                             @JsonProperty("like") Map<String, String> like) {

    public When compile() {
        Entry<String, String> eqEntry = eq.entrySet().stream()
                .map(e -> ((e.getKey().equals("data.type") || e.getKey().equals("type"))) ? e
                        : null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing data.type or type data"));

        Map<String, Pattern> likeElements = like.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> Pattern.compile(e.getValue())));

        return new When(eqEntry.getKey(), eqEntry.getValue(), likeElements);
    }
}

