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

    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/%s/filing-history/%s/internal";

    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final ResponseHandler responseHandler;

    public FilingHistoryApiClient(Supplier<InternalApiClient> internalApiClientFactory, ResponseHandler responseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
    }

    public void upsertFilingHistory(InternalFilingHistoryApi requestBody) {
        InternalApiClient client = internalApiClientFactory.get();
        client.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        final String formattedUri = PUT_REQUEST_URI.formatted(
                requestBody.getInternalData().getCompanyNumber(),
                requestBody.getExternalData().getTransactionId());

        try {
            client.privateDeltaResourceHandler()
                    .putFilingHistory(formattedUri, requestBody)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        } catch (IllegalArgumentException ex) {
            responseHandler.handle(ex);
        }
    }
}
