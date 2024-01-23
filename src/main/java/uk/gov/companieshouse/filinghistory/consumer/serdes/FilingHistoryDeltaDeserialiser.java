package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryDeltaDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ObjectMapper objectMapper;

    FilingHistoryDeltaDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FilingHistoryDelta deserialiseFilingHistoryDelta(String data) {
        try {
            return objectMapper.readValue(data, FilingHistoryDelta.class);
        } catch (JsonProcessingException e) {
            LOGGER.errorContext("Unable to parse message payload data", e, DataMapHolder.getLogMap());
            throw new NonRetryableException("Unable to parse message payload data", e);
        }
    }
}
