package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CapitalCaptor {

    private static final Pattern TREASURY_PATTERN = Pattern.compile("treasury", Pattern.CASE_INSENSITIVE);
    private final ObjectMapper objectMapper;
    private final FormatNumber formatNumber;
    private final FormatDate formatDate;

    public CapitalCaptor(ObjectMapper objectMapper, FormatNumber formatNumber, FormatDate formatDate) {
        this.objectMapper = objectMapper;
        this.formatNumber = formatNumber;
        this.formatDate = formatDate;
    }

    /**
     * Transforms a source description into a CapitalCaptures object representing the different captured capital fields
     * from the source description. E.g. the description 'Statement of Capital gbp 1000 03/02/16' has three different
     * fields to be captured: the currency GPP, the figure 1000 and the date 03/02/16. All of which when combined form a
     * single element within the resultant captures array.
     *
     * @param extract           The regex extract to be used in comparison with the sourceDescription.
     * @param altDescription    An optional alternate hyphenated description string.
     * @param sourceDescription The incoming description to be transformed into capital or alt capital arrays.
     * @return CapitalCaptures representing the two arrays of captured capital fields from the source description.
     */
    CapitalCaptures captureCapital(Pattern extract, String altDescription, String sourceDescription) {
        Matcher matcher = extract.matcher(sourceDescription);

        ArrayNode captures = objectMapper.createArrayNode();
        ArrayNode altCaptures = objectMapper.createArrayNode();

        while (matcher.find()) {
            CurrentCaptures currentCaptures = buildCurrentCaptures(matcher, sourceDescription);

            if (currentCaptures.currentlyMatchedGroups().contains("capitalAltDesc")) {
                currentCaptures.currentCapitalElement().put("description", altDescription);
                altCaptures.add(currentCaptures.currentCapitalElement());
            } else {
                captures.add(currentCaptures.currentCapitalElement());
            }
        }
        return new CapitalCaptures(captures, altCaptures);
    }

    private CurrentCaptures buildCurrentCaptures(Matcher matcher, final String sourceDescription) {
        ObjectNode capture = objectMapper.createObjectNode();

        List<String> currentlyMatchedGroups = new ArrayList<>();

        matcher.namedGroups().forEach((key, value) -> {
            if (matcher.group(value) != null) {
                currentlyMatchedGroups.add(key);
                switch (key) {
                    case "capitalDate" -> {
                        if (TREASURY_PATTERN.matcher(sourceDescription).find()) {
                            capture.put("date", formatDate.format(matcher.group(value)));
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
        return new CurrentCaptures(capture, currentlyMatchedGroups);
    }

    private record CurrentCaptures(ObjectNode currentCapitalElement, List<String> currentlyMatchedGroups) {

    }
}
