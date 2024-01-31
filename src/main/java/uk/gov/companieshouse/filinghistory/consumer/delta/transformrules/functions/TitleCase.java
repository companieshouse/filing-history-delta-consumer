package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class TitleCase implements Transformer {

    private static final Pattern IDENTIFYING_WORDS_PATTERN = Pattern.compile(
            "(\\p{L}[\\p{L}']*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FIND_FIRST_WORD_PATTERN = Pattern.compile("^(\\p{L}[\\p{L}']*)");
    private static final Pattern FIND_LAST_WORD_PATTERN = Pattern.compile("(\\p{L}[\\p{L}']*)$");
    private static final Pattern OPENING_PARENTHESIS = Pattern.compile("[(](\\p{L}[\\p{L}']*)");
    private static final Pattern CLOSING_PARENTHESIS = Pattern.compile("(\\p{L}[\\p{L}']*)\\)");
    private static final Pattern COLON = Pattern.compile("([:;]\\s+)(\\p{L}[\\p{L}']*)");

    private static final Set<String> STOP_WORDS = Set.of("A", "AN", "AT",
            "AS", "AND", "ARE", "BUT", "BY", "ERE", "FOR", "FROM", "IN", "INTO", "IS", "OF", "ON",
            "ONTO", "OR", "OVER", "PER", "THE", "TO", "THAT", "THAN", "UNTIL", "UNTO", "UPON",
            "VIA", "WITH", "WHILE", "WHILST", "WITHIN", "WITHOUT");

    private final ObjectMapper objectMapper;

    public TitleCase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> captureGroups) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, transformTitleCase(arguments.getFirst()));
    }

    String transformTitleCase(String jsonFieldWeWantToTransform) {
        if (StringUtils.isEmpty(jsonFieldWeWantToTransform)) {
            return jsonFieldWeWantToTransform;
        }
        jsonFieldWeWantToTransform = jsonFieldWeWantToTransform.toUpperCase(Locale.UK);
        jsonFieldWeWantToTransform = mapToken(IDENTIFYING_WORDS_PATTERN, jsonFieldWeWantToTransform,
                (word, matcher)
                        -> STOP_WORDS.contains(word) ? word.toLowerCase(Locale.UK) :
                        WordUtils.capitalizeFully(word), true);
        jsonFieldWeWantToTransform = mapToken(FIND_FIRST_WORD_PATTERN, jsonFieldWeWantToTransform,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        jsonFieldWeWantToTransform = mapToken(FIND_LAST_WORD_PATTERN, jsonFieldWeWantToTransform,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        jsonFieldWeWantToTransform = mapToken(OPENING_PARENTHESIS, jsonFieldWeWantToTransform,
                (token, matcher) ->
                        "(" + WordUtils.capitalizeFully(matcher.group(1)), false);
        jsonFieldWeWantToTransform = mapToken(CLOSING_PARENTHESIS, jsonFieldWeWantToTransform,
                (token, matcher) ->
                        WordUtils.capitalizeFully(matcher.group(1)) + ")", false);
        jsonFieldWeWantToTransform = mapToken(COLON, jsonFieldWeWantToTransform, (token, matcher) ->
                matcher.group(1) + WordUtils.capitalizeFully(matcher.group(2)), false);
        return jsonFieldWeWantToTransform.trim();
    }
}
