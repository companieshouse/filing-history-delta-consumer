package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Component;
@Component
public class SentenceCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        ObjectNode workingNode = outputNode;

        String finalField = getFinalField(objectMapper, field, outputNode);
        String nodeText = outputNode.at("/" + arguments.getFirst().replace(".", "/"))
                .textValue();

        // TODO Apply Perl sentence_case transformation to node text
        String transformedText = "TODO: Sentence case: " + nodeText;

        outputNode.put(finalField, transformedText);
    }

    public String transformSentenceCase(String nodeText) {
        if(StringUtil.isEmpty(nodeText)){
            return nodeText;
        }
        nodeText = nodeText.toUpperCase(Locale.UK);
//        jsonFieldWeWantToTransform = mapToken(IDENTIFYING_WORDS_PATTERN, jsonFieldWeWantToTransform, (word, matcher)
//                -> STOP_WORDS.contains(word) ? word.toLowerCase(Locale.UK) :
//                WordUtils.capitalizeFully(word), true);
//        jsonFieldWeWantToTransform = mapToken(FIND_FIRST_WORD_PATTERN, jsonFieldWeWantToTransform, //this first word pattern will work apart from instances when the string does not
//                // begin with a word, in which case you will need a different regex.
//                (word, matcher) -> WordUtils.capitalizeFully(word), false);
//        jsonFieldWeWantToTransform = mapToken(FIND_LAST_WORD_PATTERN, jsonFieldWeWantToTransform,
//                (word, matcher) -> WordUtils.capitalizeFully(word), false);
//        jsonFieldWeWantToTransform = mapToken(OPENING_PARENTHESIS, jsonFieldWeWantToTransform, (token, matcher) ->
//                "(" + org.apache.commons.text.WordUtils.capitalizeFully(matcher.group(1)), false);
//        jsonFieldWeWantToTransform = mapToken(CLOSING_PARENTHESIS, jsonFieldWeWantToTransform, (token, matcher) ->
//                org.apache.commons.text.WordUtils.capitalizeFully(matcher.group(1)) + ")", false);
//        jsonFieldWeWantToTransform = mapToken(COLON, jsonFieldWeWantToTransform, (token, matcher) ->
//                matcher.group(1) + org.apache.commons.text.WordUtils.capitalizeFully(matcher.group(2)), false);



        return  nodeText.trim();
    }
}
