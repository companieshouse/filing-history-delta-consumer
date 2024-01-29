package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Transformer {

    void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue);

    default String getFinalField(ObjectMapper objectMapper, String field, ObjectNode outputNode){
        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at("/" + fields[i]);
        }

        return fields[fields.length - 1];
    }

    static String mapToken(Pattern pattern,
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
