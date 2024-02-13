package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class TitleCase extends AbstractTransformer {

    private static final Pattern IDENTIFYING_WORDS_PATTERN = Pattern.compile(
            "(\\p{L}[\\p{L}']*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FIND_FIRST_WORD_PATTERN = Pattern.compile("^(\\p{L}[\\p{L}']*)");
    private static final Pattern FIND_LAST_WORD_PATTERN = Pattern.compile("(\\p{L}[\\p{L}']*)$");
    private static final Pattern OPENING_PARENTHESIS = Pattern.compile("[(](\\p{L}[\\p{L}']*)");
    private static final Pattern CLOSING_PARENTHESIS = Pattern.compile("(\\p{L}[\\p{L}']*)\\)");
    private static final Pattern COLON = Pattern.compile("([:;]\\s+)(\\p{L}[\\p{L}']*)");
    private static final Pattern ABBREVIATIONS = Pattern.compile("\\b(\\p{L})[.]");

    private static final Set<String> STOP_WORDS = Set.of("A", "AN", "AT",
            "AS", "AND", "ARE", "BUT", "BY", "ERE", "FOR", "FROM", "IN", "INTO", "IS", "OF", "ON",
            "ONTO", "OR", "OVER", "PER", "THE", "TO", "THAT", "THAN", "UNTIL", "UNTO", "UPON",
            "VIA", "WITH", "WHILE", "WHILST", "WITHIN", "WITHOUT");

    public TitleCase(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        String targetValue = getFieldToTransform(source, arguments, context);
        target.objectNode().put(target.fieldKey(), transformTitleCase(targetValue));
    }

    String transformTitleCase(String field) {
        if (StringUtils.isEmpty(field)) {
            return field;
        }
        field = field.toUpperCase(Locale.UK);
        field = mapToken(IDENTIFYING_WORDS_PATTERN, field,
                (word, matcher)
                        -> STOP_WORDS.contains(word) ? word.toLowerCase(Locale.UK) :
                        WordUtils.capitalizeFully(word), true);
        field = mapToken(FIND_FIRST_WORD_PATTERN, field,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        field = mapToken(FIND_LAST_WORD_PATTERN, field,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        field = mapToken(OPENING_PARENTHESIS, field,
                (token, matcher) ->
                        "(" + WordUtils.capitalizeFully(matcher.group(1)), false);
        field = mapToken(CLOSING_PARENTHESIS, field,
                (token, matcher) ->
                        WordUtils.capitalizeFully(matcher.group(1)) + ")", false);
        field = mapToken(COLON, field, (token, matcher) ->
                matcher.group(1) + WordUtils.capitalizeFully(matcher.group(2)), false);
        field = mapToken(ABBREVIATIONS, field, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK) + ".", true);
        return field.trim();
    }
}
