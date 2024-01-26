package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerUtils.toJsonPtr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ProcessCapital {

    private static final Pattern TREASURY_PATTERN = Pattern.compile("treasury", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final FormatNumber formatNumber;

    public ProcessCapital(FormatNumber formatNumber) {
        this.formatNumber = formatNumber;
    }

    public void transform(JsonNode source, ObjectNode outputNode,
            String fieldPath, Pattern extract, String altDescription) {

        String[] fields = fieldPath.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at("/" + fields[i]);
        }

        String sourceDescriptionPath = "/%s".formatted(fieldPath).replace(".", "/");
        String sourceDescription = source.at(sourceDescriptionPath).textValue();

        Matcher matcher = extract.matcher(sourceDescription);

        ArrayNode captures = objectMapper.createArrayNode();
        ArrayNode altCaptures = objectMapper.createArrayNode();

        while (matcher.find()) {
            Map<String, Integer> namedGroups = matcher.namedGroups();

            ObjectNode capture = objectMapper.createObjectNode();

            List<String> currentlyMatchedGroups = new ArrayList<>();

            namedGroups.forEach((key, value) -> {
                if (matcher.group(value) != null) {
                    currentlyMatchedGroups.add(key);
                    switch (key) {
                        case "capitalDate" -> {
                            if (TREASURY_PATTERN.matcher(matcher.group()).find()) {
                                capture.put("date", matcher.group(value));
                            }
                        }
                        case "capitalCurrency" -> capture.put("currency", StringUtils.upperCase(matcher.group(value)));
                        case "capitalFigure" -> capture.put("figure", formatNumber.apply(matcher.group(value)));
                        default -> {
                            // do nothing with the capitalDesc group
                            // capitalAltDesc used last to determine which array node push to
                        }
                    }
                }
            });

            if (currentlyMatchedGroups.contains("capitalAltDesc")) {
                capture.put("description", altDescription);
                altCaptures.add(capture);
            } else {
                captures.add(capture);
            }
        }

        outputNode.putIfAbsent("description_values", objectMapper.createObjectNode());
        ObjectNode descriptionValues = (ObjectNode) outputNode.at(toJsonPtr("description_values"));

        descriptionValues
                .putArray("capital")
                .addAll(captures);

        if (!altCaptures.isEmpty()) {
            descriptionValues.
                    putArray("alt_capital")
                    .addAll(altCaptures);
        }
    }
}
