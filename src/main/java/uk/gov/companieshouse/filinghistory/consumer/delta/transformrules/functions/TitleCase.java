package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jetty.util.StringUtil;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

public class TitleCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern IDENTIFYING_WORDS_PATTERN = Pattern.compile(
            "(\\p{L}[\\p{L}']*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERL_PATTERN = Pattern.compile("((?:\\pL|&[a-z]+;)(?:[\\pL']|&[a-z]+;)*)");

    private static final Pattern FIND_FIRST_WORD_PATTERN = Pattern.compile("^(\\p{L}[\\p{L}']*)");

    private static final Pattern FIND_LAST_WORD_PATTERN = Pattern.compile("(\\p{L}[\\p{L}']*)$");

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

        outputNode.put(finalField, "Title case: " + transformTitleCase(arguments.getFirst()));
    }

    public String transformTitleCase(String jsonFieldWeWantToTransform) {
        if(StringUtil.isEmpty(jsonFieldWeWantToTransform)){
            return jsonFieldWeWantToTransform;
        }
        jsonFieldWeWantToTransform = jsonFieldWeWantToTransform.toUpperCase(Locale.UK);
        jsonFieldWeWantToTransform = mapToken(IDENTIFYING_WORDS_PATTERN, jsonFieldWeWantToTransform, (word, matcher)
                -> STOP_WORDS.contains(word) ? word.toLowerCase(Locale.UK) :
                WordUtils.capitalizeFully(word), true);
        jsonFieldWeWantToTransform = mapToken(FIND_FIRST_WORD_PATTERN, jsonFieldWeWantToTransform, //this first word pattern will work apart from instances when the string does not
                // begin with a word, in which case you will need a different regex.
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        jsonFieldWeWantToTransform = mapToken(FIND_LAST_WORD_PATTERN, jsonFieldWeWantToTransform,
                (word, matcher) -> WordUtils.capitalizeFully(word), false);
        jsonFieldWeWantToTransform = mapToken(COLON, jsonFieldWeWantToTransform, (token, matcher) ->
                matcher.group(1) + org.apache.commons.text.WordUtils.capitalizeFully(matcher.group(2)), false);


        return jsonFieldWeWantToTransform.trim();
    }

    private static String mapToken(Pattern pattern,
            String word,
            BiFunction<String, Matcher, String> matchRemappingFunction,
            boolean global) {
        Matcher matcher = pattern.matcher(word);
        StringBuilder result = new StringBuilder();
        int start;
        int end;
        int prevEnd = 0;
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            if (start > 0) {
                result.append(word.substring(prevEnd, start));
            }
            result.append(matchRemappingFunction.apply(
                    word.substring(start, end), matcher));
            prevEnd = end;
            if (!global) {
                break;
            }
        }
        result.append(word.substring(prevEnd));
        return result.toString();
    }

}
