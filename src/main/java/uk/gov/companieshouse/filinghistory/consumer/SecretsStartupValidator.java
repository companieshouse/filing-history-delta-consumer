package uk.gov.companieshouse.filinghistory.consumer;

import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

@Component
class SecretsStartupValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private static final String MISSING_VARIABLES_ERROR =
            "Refusing to start with default/blank secrets for TRANSACTION_ID_SALT, FILING_HISTORY_API_KEY and/or API_LOCAL_URL";

    SecretsStartupValidator(Environment env) {
        LOGGER.info("Check for presence of non-defaulted environment variables");

        String salt = env.getProperty("TRANSACTION_ID_SALT", "");
        String apiKey = env.getProperty("FILING_HISTORY_API_KEY", "");
        String apiUrl = env.getProperty("API_LOCAL_URL", "");

        if (salt.isBlank() || apiKey.isBlank() || apiUrl.isBlank()) {
            LOGGER.error(MISSING_VARIABLES_ERROR);

            throw new IllegalStateException(MISSING_VARIABLES_ERROR);
        }
    }
}
