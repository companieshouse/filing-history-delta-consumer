package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static java.time.ZoneOffset.UTC;
import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FormatDate extends AbstractTransformer {

    private static final Logger logger = LoggerFactory.getLogger(NAMESPACE);

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter SLASHES_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter TWO_YEAR_SLASHES_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("d/MM/")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 1970)
            .toFormatter();
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$");

    public FormatDate(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        target.objectNode().put(target.fieldKey(), formatIfRequired(target.fieldValue()));
    }

    public String format(String nodeText) {
        return doFormat(nodeText);
    }

    private String formatIfRequired(String nodeText) {
        if (nodeText == null || ISO_DATE_PATTERN.matcher(nodeText).matches()) {
            return nodeText;
        }
        return doFormat(nodeText);
    }

    private String doFormat(String nodeText) {

        try {
            if (StringUtils.isEmpty(nodeText)) {
                return nodeText;
            }
            ZonedDateTime nodeTextAsDate;
            boolean hasSlash = nodeText.contains("/");
            if (hasSlash && nodeText.length() > 8) {
                nodeTextAsDate = LocalDate.parse(nodeText, SLASHES_FORMATTER).atStartOfDay(UTC);
            } else if (nodeText.length() == 8 && !hasSlash) {
                nodeTextAsDate = LocalDate.parse(nodeText, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay(UTC);
            } else if (nodeText.length() == 14) {
                nodeTextAsDate = LocalDateTime.parse(nodeText, INPUT_FORMATTER).atZone(UTC);
            } else {
                nodeTextAsDate = LocalDate.parse(nodeText, TWO_YEAR_SLASHES_FORMATTER).atStartOfDay(UTC);
            }
            return DateTimeFormatter.ISO_INSTANT.format(nodeTextAsDate.toInstant());
        } catch (Exception e) {
            logger.error("Failed to parse date: %s. %s".formatted(nodeText, e.getMessage()), e, DataMapHolder.getLogMap());
            throw new NonRetryableException("Failed to parse date string %s".formatted(nodeText), e);
        }
    }
}
