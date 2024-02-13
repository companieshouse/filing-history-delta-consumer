package uk.gov.companieshouse.filinghistory.consumer.apiclient;

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
    private static final String API_ERROR_RESPONSE_MESSAGE = "HTTP response code %d when upserting filing history";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Failed to upsert filing history due to invalid URI";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "Failed to upsert filing history due illegal argument. %s";

    public void handle(ApiErrorResponseException ex) {
        final int statusCode = ex.getStatusCode();
        if (HttpStatus.valueOf(statusCode).is5xxServerError()) {
            LOGGER.info(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), DataMapHolder.getLogMap());
            throw new RetryableException(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), ex);
        } else {
            LOGGER.error(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), DataMapHolder.getLogMap());
            throw new NonRetryableException(API_ERROR_RESPONSE_MESSAGE.formatted(statusCode), ex);
        }
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new NonRetryableException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }

    public void handle(IllegalArgumentException ex) {
        final String causeMessage = ex.getCause() != null ? "; %s".formatted(ex.getCause().getMessage()) : "";
        LOGGER.info(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE.formatted(causeMessage), DataMapHolder.getLogMap());
        throw new RetryableException(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE.formatted(causeMessage), ex);
    }
}
