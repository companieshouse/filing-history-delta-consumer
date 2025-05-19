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
    public List<String> formTypeBlockList(@Value("${associated-filings.block-list}") final String file) {
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(file));
        if (stream == null) {
            LOGGER.error("Block list file not found: [%s]".formatted(file), DataMapHolder.getLogMap());
            throw new NonRetryableException("Block list file not found: [%s]".formatted(file));
        }
        Scanner scanner = new Scanner(stream);
        List<String> blockList = new ArrayList<>();
        while (scanner.hasNextLine()) {
            blockList.add(scanner.nextLine().split(",")[0]);
        }
        return blockList;
    }
}
