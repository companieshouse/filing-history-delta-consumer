package uk.gov.companieshouse.filinghistory.consumer.delta;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;

@Component
public class LinksMapper {

    public FilingHistoryItemDataLinks map(final String companyNumber, final String transactionId) {
        if (StringUtils.isBlank(companyNumber) || StringUtils.isBlank(transactionId)) {
            throw new IllegalArgumentException("Cannot map self link with null or empty data");
        }
        return new FilingHistoryItemDataLinks()
                .self("/company/%s/filing-history/%s".formatted(companyNumber, transactionId));
    }
}
