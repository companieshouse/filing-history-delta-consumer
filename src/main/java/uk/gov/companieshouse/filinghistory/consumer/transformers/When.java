package uk.gov.companieshouse.filinghistory.consumer.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.regex.Pattern;

public record When(String field, String formType, Map<String, Pattern> like) {

    public boolean match(JsonNode putRequest) {
        String deltaFormType = putRequest.get(field).textValue();

        if (formType.equals(deltaFormType)) {
            return like.entrySet().stream()
                    .allMatch(e -> {
                        String deltaLikeField = putRequest.get(e.getKey()).textValue();
                        return e.getValue().matcher(deltaLikeField).matches();
                    });
        }
        return false;
    }
}
