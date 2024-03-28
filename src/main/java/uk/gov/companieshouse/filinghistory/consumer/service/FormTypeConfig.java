package uk.gov.companieshouse.filinghistory.consumer.service;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class FormTypeConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Bean
    public List<String> formTypeBlacklist() throws IOException {
        final String file = "src/main/resources/associated_filings_blacklist.csv";
        List<String> blacklist = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                blacklist.addAll(Arrays.asList(values));
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to read from CSV file", ex, DataMapHolder.getLogMap());
            throw ex;
        }
        return blacklist;
    }
}
