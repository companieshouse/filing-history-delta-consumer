package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;

@Component
public class LinksMapper {

    public FilingHistoryItemDataLinks map(JsonNode topLevelNode) {
        return null;
    }
}
