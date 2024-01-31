package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.filinghistory.request.PrivateFilingHistoryPut;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;

@ExtendWith(MockitoExtension.class)
class FilingHistoryApiClientTest {

    private static final String PRE_FORMAT_URI = "/company/%s/filing-history/%s";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "MzA0Mzk3MjY3NXNhbHQ";
    private static final String REQUEST_ID = "request_id";

    @InjectMocks
    private FilingHistoryApiClient filingHistoryApiClient;

    @Mock
    private Supplier<InternalApiClient> internalApiClientFactory;
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateFilingHistoryPut privateFilingHistoryPut;

    @Test
    void shouldSendSuccessfulPutRequest() throws Exception {
        // given
        doReturn(internalApiClient).when(internalApiClientFactory).get();
        doReturn(apiClient).when(internalApiClient).getHttpClient();
        doReturn(privateDeltaResourceHandler).when(internalApiClient).privateDeltaResourceHandler();
        doReturn(privateFilingHistoryPut).when(privateDeltaResourceHandler).putFilingHistory(any(), any());

        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);
        InternalFilingHistoryApi requestBody = buildRequiredRequestBody(expectedUri);
        DataMapHolder.get().requestId(REQUEST_ID);


        // when
        filingHistoryApiClient.upsertFilingHistory(requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putFilingHistory(expectedUri, requestBody);
        verify(privateFilingHistoryPut).execute();
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        doReturn(internalApiClient).when(internalApiClientFactory).get();
        doReturn(apiClient).when(internalApiClient).getHttpClient();
        doReturn(privateDeltaResourceHandler).when(internalApiClient).privateDeltaResourceHandler();
        doReturn(privateFilingHistoryPut).when(privateDeltaResourceHandler).putFilingHistory(any(), any());
        doThrow(exceptionClass).when(privateFilingHistoryPut).execute();

        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);
        InternalFilingHistoryApi requestBody = buildRequiredRequestBody(expectedUri);
        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        filingHistoryApiClient.upsertFilingHistory(requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putFilingHistory(expectedUri, requestBody);
        verify(privateFilingHistoryPut).execute();
        verify(responseHandler).handle(any(), any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        doReturn(internalApiClient).when(internalApiClientFactory).get();
        doReturn(apiClient).when(internalApiClient).getHttpClient();
        doReturn(privateDeltaResourceHandler).when(internalApiClient).privateDeltaResourceHandler();
        doReturn(privateFilingHistoryPut).when(privateDeltaResourceHandler).putFilingHistory(any(), any());
        doThrow(exceptionClass).when(privateFilingHistoryPut).execute();

        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);
        InternalFilingHistoryApi requestBody = buildRequiredRequestBody(expectedUri);
        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        filingHistoryApiClient.upsertFilingHistory(requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putFilingHistory(expectedUri, requestBody);
        verify(privateFilingHistoryPut).execute();
        verify(responseHandler).handle(any(), any(exceptionClass));
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<IllegalArgumentException> exceptionClass = IllegalArgumentException.class;

        doReturn(internalApiClient).when(internalApiClientFactory).get();
        doReturn(apiClient).when(internalApiClient).getHttpClient();
        doReturn(privateDeltaResourceHandler).when(internalApiClient).privateDeltaResourceHandler();
        doReturn(privateFilingHistoryPut).when(privateDeltaResourceHandler).putFilingHistory(any(), any());
        doThrow(exceptionClass).when(privateFilingHistoryPut).execute();

        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);
        InternalFilingHistoryApi requestBody = buildRequiredRequestBody(expectedUri);
        DataMapHolder.get().requestId(REQUEST_ID);

        // when
        filingHistoryApiClient.upsertFilingHistory(requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putFilingHistory(expectedUri, requestBody);
        verify(privateFilingHistoryPut).execute();
        verify(responseHandler).handle(any(), any(exceptionClass));
    }

    private static InternalFilingHistoryApi buildRequiredRequestBody(final String selfLink) {
        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .links(new FilingHistoryItemDataLinks()
                                .self(selfLink)));
    }
}
