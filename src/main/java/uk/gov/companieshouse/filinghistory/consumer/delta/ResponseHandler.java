package uk.gov.companieshouse.filinghistory.consumer.delta;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.exception.RetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public void handle(final String message, ApiErrorResponseException ex) {
        if (HttpStatus.valueOf(ex.getStatusCode()).is5xxServerError()) {
            LOGGER.info(message, DataMapHolder.getLogMap());
            throw new RetryableException(message, ex);
        } else {
            LOGGER.error(message, DataMapHolder.getLogMap());
            throw new NonRetryableException(message, ex);
        }
    }

    public void handle(final String message, URIValidationException ex) {
        LOGGER.error(message, DataMapHolder.getLogMap());
        throw new NonRetryableException(message, ex);
    }

    public void handle(final String message, IllegalArgumentException ex) {
        final String causeMessage = ex.getCause() != null ? "; %s".formatted(ex.getCause().getMessage()) : "";
        LOGGER.info(message + causeMessage, DataMapHolder.getLogMap());
        throw new RetryableException(message, ex);
    }
}
