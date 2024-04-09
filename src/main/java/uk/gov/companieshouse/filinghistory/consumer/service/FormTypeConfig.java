package uk.gov.companieshouse.filinghistory.consumer.service;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class FormTypeConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Bean
    public List<String> formTypeBlacklist(@Value("${associated-filings.blacklist}") final String file) {
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(file));
        if (stream == null) {
            LOGGER.error("Blacklist file not found: [%s]".formatted(file), DataMapHolder.getLogMap());
            throw new NonRetryableException("Blacklist file not found: [%s]".formatted(file));
        }
        Scanner scanner = new Scanner(stream);
        List<String> blacklist = new ArrayList<>();
        while (scanner.hasNextLine()) {
            blacklist.add(scanner.nextLine().split(",")[0]);
        }
        return blacklist;
    }
}
