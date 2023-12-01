package uk.gov.companieshouse.filinghistory.consumer.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
