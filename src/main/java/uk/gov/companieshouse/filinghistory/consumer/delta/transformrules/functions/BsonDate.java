package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class BsonDate implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter SLASHES_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, "TODO: BSON date: " + arguments.getFirst());
    }

    String transformBsonDate(String nodeText) {
        if (StringUtils.isEmpty(nodeText)) {
            return nodeText;
        }
        Instant nodeTextAsDate;
        if (nodeText.length() == 8) {
            nodeTextAsDate = LocalDate.parse(nodeText, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay(
                    ZoneOffset.UTC).toInstant();
        } else if (nodeText.contains("/")) {
            nodeTextAsDate = LocalDate.parse(nodeText, SLASHES_FORMATTER).atStartOfDay(ZoneOffset.UTC).toInstant();
        } else if (nodeText.length() == 14) {
            nodeTextAsDate = LocalDateTime.parse(nodeText, INPUT_FORMATTER).atZone(ZoneOffset.UTC).toInstant();
        } else {
            throw new IllegalArgumentException("Unrecognised Bson Date format");
        }
        return DateTimeFormatter.ISO_INSTANT.format(nodeTextAsDate);
    }

}
