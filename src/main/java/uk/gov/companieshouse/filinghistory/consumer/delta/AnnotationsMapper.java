package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;

@Component
public class AnnotationsMapper {

    public List<FilingHistoryItemDataAnnotations> map(JsonNode topLevelNode) {
        return Collections.emptyList();
    }
}
