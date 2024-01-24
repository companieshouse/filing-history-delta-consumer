package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class TitleCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern IDENTIFYING_WORDS_PATTERN = Pattern.compile(
            "(\\p{L}[\\p{L}']*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERL_PATTERN = Pattern.compile("((?:\\pL|&[a-z]+;)(?:[\\pL']|&[a-z]+;)*)");
    private static final Pattern FIND_FIRST_WORD_PATTERN = Pattern.compile("^(\\p{L}[\\p{L}']*)");
    private static final Pattern FIND_LAST_WORD_PATTERN = Pattern.compile("(\\p{L}[\\p{L}']*)$");
    private static final Pattern OPENING_PARENTHESIS = Pattern.compile("[(](\\p{L}[\\p{L}']*)");
    private static final Pattern CLOSING_PARENTHESIS = Pattern.compile("(\\p{L}[\\p{L}']*)[)]");
    private static final Pattern COLON = Pattern.compile("([:;]\\s+)(\\p{L}[\\p{L}']*)");

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList("A", "AN", "AT",
            "AS", "AND", "ARE", "BUT", "BY", "ERE", "FOR", "FROM", "IN", "INTO", "IS", "OF", "ON",
            "ONTO", "OR", "OVER", "PER", "THE", "TO", "THAT", "THAN", "UNTIL", "UNTO", "UPON",
            "VIA", "WITH", "WHILE", "WHILST", "WITHIN", "WITHOUT"));

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> captureGroups) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, "Title case: " + arguments.getFirst());
    }

     String transformTitleCase(String jsonFieldWeWantToTransform) {
        if(StringUtils.isEmpty(jsonFieldWeWantToTransform)){
            return jsonFieldWeWantToTransform;
        }
        jsonFieldWeWantToTransform = jsonFieldWeWantToTransform.toUpperCase(Locale.UK);
        jsonFieldWeWantToTransform = Transformer.mapToken(IDENTIFYING_WORDS_PATTERN, jsonFieldWeWantToTransform, (word, matcher)
                -> STOP_WORDS.contains(word) ? word.toLowerCase(Locale.UK) :
                WordUtils.capitalizeFully(word), true);
        jsonFieldWeWantToTransform = Transformer.mapToken(FIND_FIRST_WORD_PATTERN, jsonFieldWeWantToTransform,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        jsonFieldWeWantToTransform = Transformer.mapToken(FIND_LAST_WORD_PATTERN, jsonFieldWeWantToTransform,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        jsonFieldWeWantToTransform = Transformer.mapToken(OPENING_PARENTHESIS, jsonFieldWeWantToTransform, (token, matcher) ->
                "(" + WordUtils.capitalizeFully(matcher.group(1)), false);
        jsonFieldWeWantToTransform = Transformer.mapToken(CLOSING_PARENTHESIS, jsonFieldWeWantToTransform, (token, matcher) ->
                WordUtils.capitalizeFully(matcher.group(1)) + ")", false);
        jsonFieldWeWantToTransform = Transformer.mapToken(COLON, jsonFieldWeWantToTransform, (token, matcher) ->
                matcher.group(1) + WordUtils.capitalizeFully(matcher.group(2)), false);
        return jsonFieldWeWantToTransform.trim();
    }
}
