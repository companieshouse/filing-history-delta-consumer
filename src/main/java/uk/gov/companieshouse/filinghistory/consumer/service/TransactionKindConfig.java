package uk.gov.companieshouse.filinghistory.consumer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.filinghistory.utils.TransactionKindService;

@Configuration
public class TransactionKindConfig {

    @Bean
    public TransactionKindService transactionKindService(@Value("${transaction-id-salt}") String transactionIdSalt) {
        return new TransactionKindService(transactionIdSalt);
    }
}
