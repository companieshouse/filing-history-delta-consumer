package uk.gov.companieshouse.filinghistory.consumer.delta;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;

@Component
public class FilingHistoryApiClient {

    private static final String FAILED_MSG = "Failed to upsert filing history for resource URI %s";
    private static final String ERROR_MSG = "HTTP response code %s when upserting filing history for resource URI %s";

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final ResponseHandler responseHandler;

    public FilingHistoryApiClient(Supplier<InternalApiClient> internalApiClientFactory, ResponseHandler responseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
    }

    public void upsertFilingHistory(InternalFilingHistoryApi requestBody) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String uri = requestBody.getExternalData().getLinks().getSelf();

        try {
            client.privateDeltaResourceHandler()
                    .putFilingHistory(uri, requestBody)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ERROR_MSG.formatted(ex.getStatusCode(), uri), ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(FAILED_MSG.formatted(uri), ex);
        } catch (IllegalArgumentException ex) {
            responseHandler.handle(FAILED_MSG.formatted(uri), ex);
        }
    }
}
