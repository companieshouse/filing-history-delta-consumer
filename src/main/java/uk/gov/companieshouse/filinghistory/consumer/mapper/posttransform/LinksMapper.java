package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class LinksMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public Links map(final String companyNumber, final String transactionId) {
        if (StringUtils.isBlank(companyNumber) || StringUtils.isBlank(transactionId)) {
            LOGGER.error("Company Number and Transaction ID must not be null", DataMapHolder.getLogMap());
            throw new IllegalArgumentException("Cannot map self link with null or empty data");
        }
        return new Links()
                .self("/company/%s/filing-history/%s".formatted(companyNumber, transactionId));
    }
}
