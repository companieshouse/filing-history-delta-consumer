package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerUtils.toJsonPtr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTransformer implements Transformer {

    private final ObjectMapper objectMapper;

    protected AbstractTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void transform(JsonNode source, ObjectNode outputNode, String field, List<String> arguments,
            Map<String, String> context) {
        TransformTarget transformTarget = getTransformTarget(field, outputNode);
        doTransform(source, transformTarget, arguments, context);
    }

    protected abstract void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context);

    protected TransformTarget getTransformTarget(String field, ObjectNode outputNode) {
        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at("/" + fields[i]);
        }

        return new TransformTarget(fields[fields.length - 1], outputNode);
    }

    protected String getFieldToTransform(JsonNode source, List<String> arguments, Map<String, String> context) {
        String sourceFieldOrCapture = arguments.getFirst();
        String captureValue = context.get(sourceFieldOrCapture);
        JsonNode sourceValue = source.at(toJsonPtr(sourceFieldOrCapture));

        String fieldToTransform;
        if (captureValue != null) {
            fieldToTransform = captureValue;
        } else if (!(sourceValue instanceof MissingNode) && sourceValue != null) {
            fieldToTransform = sourceValue.textValue();
        } else {
            throw new IllegalArgumentException("Failed to find capture or source field to transform");
        }
        return fieldToTransform;
    }

    protected String mapToken(Pattern pattern,
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
                result.append(word, prevEnd, start);
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
