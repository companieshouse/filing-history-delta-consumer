package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static java.time.ZoneOffset.UTC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FormatDate extends AbstractTransformer {

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter SLASHES_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter TWO_YEAR_SLASHES_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("d/MM/")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 1970)
            .toFormatter();

    public FormatDate(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        String targetValue = getFieldToTransform(source, arguments, context);
        target.objectNode().put(target.fieldKey(), format(targetValue));
    }

    public String format(String nodeText) {
        if (StringUtils.isEmpty(nodeText)) {
            return nodeText;
        }
        Instant nodeTextAsDate;
        boolean hasSlash = nodeText.contains("/");
        if (hasSlash && nodeText.length() > 8) {
            nodeTextAsDate = LocalDate.parse(nodeText, SLASHES_FORMATTER).atStartOfDay(UTC).toInstant();
        } else if (nodeText.length() == 8 && !hasSlash) {
            nodeTextAsDate = LocalDate.parse(nodeText, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay(UTC).toInstant();
        } else if (nodeText.length() == 14) {
            nodeTextAsDate = LocalDateTime.parse(nodeText, INPUT_FORMATTER).atZone(UTC).toInstant();
        } else {
            nodeTextAsDate = LocalDate.parse(nodeText, TWO_YEAR_SLASHES_FORMATTER).atStartOfDay(UTC).toInstant();
        }
        return DateTimeFormatter.ISO_INSTANT.format(nodeTextAsDate);
    }

}
