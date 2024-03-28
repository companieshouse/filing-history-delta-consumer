package uk.gov.companieshouse.filinghistory.consumer.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FormTypeConfig {

    @Bean
    public FormTypeService formTypeService() {
        final String file = "src/main/resources/associated_filings_blacklist.csv";
        List<String> blacklist = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                blacklist.addAll(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Throw appropriate exception
        }
        return new FormTypeService(blacklist);
    }
}
