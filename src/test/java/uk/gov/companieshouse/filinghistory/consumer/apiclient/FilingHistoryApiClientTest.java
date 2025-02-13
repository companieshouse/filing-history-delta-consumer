package uk.gov.companieshouse.filinghistory.consumer.apiclient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.filinghistory.request.PrivateFilingHistoryDelete;
import uk.gov.companieshouse.api.handler.delta.filinghistory.request.PrivateFilingHistoryPut;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.consumer.service.DeleteApiClientRequest;

@ExtendWith(MockitoExtension.class)
class FilingHistoryApiClientTest {

    private static final String PRE_FORMAT_URI = "/company/%s/filing-history/%s/internal";
    private static final String PRE_FORMAT_DELETE_URI = "/company/%s/filing-history/%s/internal";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "MzA0Mzk3MjY3NXNhbHQ";
    private static final String ENTITY_ID = "1234567891";
    private static final String PARENT_ENTITY_ID = "2234567891";
    private static final String REQUEST_ID = "request_id";
    private static final String DELTA_AT = "20240219123045999999";
    private static final DeleteApiClientRequest API_CLIENT_REQUEST =
            DeleteApiClientRequest.builder()
                    .transactionId(TRANSACTION_ID)
                    .companyNumber(COMPANY_NUMBER)
                    .entityId(ENTITY_ID)
                    .deltaAt(DELTA_AT)
                    .parentEntityId(PARENT_ENTITY_ID)
                    .build();

    @InjectMocks
    private FilingHistoryApiClient filingHistoryApiClient;

    @Mock
    private Supplier<InternalApiClient> internalApiClientFactory;
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private InternalFilingHistoryApi requestBody;
    @Mock
    private ExternalData externalData;
    @Mock
    private InternalData internalData;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateFilingHistoryPut privateFilingHistoryPut;
    @Mock
    private PrivateFilingHistoryDelete privateFilingHistoryDelete;

    @Test
    void shouldSendSuccessfulPutRequest() throws Exception {
        // given
        when(requestBody.getExternalData()).thenReturn(externalData);
        when(externalData.getTransactionId()).thenReturn(TRANSACTION_ID);
        when(requestBody.getInternalData()).thenReturn(internalData);
        when(internalData.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putFilingHistory(any(), any())).thenReturn(privateFilingHistoryPut);

        DataMapHolder.get().requestId(REQUEST_ID);
        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);

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

        when(requestBody.getExternalData()).thenReturn(externalData);
        when(externalData.getTransactionId()).thenReturn(TRANSACTION_ID);
        when(requestBody.getInternalData()).thenReturn(internalData);
        when(internalData.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putFilingHistory(any(), any())).thenReturn(privateFilingHistoryPut);
        when(privateFilingHistoryPut.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);
        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        filingHistoryApiClient.upsertFilingHistory(requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putFilingHistory(expectedUri, requestBody);
        verify(privateFilingHistoryPut).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingPutRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(requestBody.getExternalData()).thenReturn(externalData);
        when(externalData.getTransactionId()).thenReturn(TRANSACTION_ID);
        when(requestBody.getInternalData()).thenReturn(internalData);
        when(internalData.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putFilingHistory(any(), any())).thenReturn(privateFilingHistoryPut);
        when(privateFilingHistoryPut.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);
        final String expectedUri = PRE_FORMAT_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        filingHistoryApiClient.upsertFilingHistory(requestBody);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).putFilingHistory(expectedUri, requestBody);
        verify(privateFilingHistoryPut).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldSendSuccessfulDeleteRequest() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteFilingHistory(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(privateFilingHistoryDelete);

        DataMapHolder.get().requestId(REQUEST_ID);
        final String expectedUri = PRE_FORMAT_DELETE_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        filingHistoryApiClient.deleteFilingHistory(API_CLIENT_REQUEST);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).deleteFilingHistory(expectedUri, DELTA_AT, ENTITY_ID, PARENT_ENTITY_ID);
        verify(privateFilingHistoryDelete).execute();
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldHandleApiErrorExceptionWhenSendingDeleteRequest() throws Exception {
        // given
        Class<ApiErrorResponseException> exceptionClass = ApiErrorResponseException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteFilingHistory(anyString(), anyString(), anyString(), anyString())).thenReturn(
                privateFilingHistoryDelete);
        when(privateFilingHistoryDelete.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);
        final String expectedUri = PRE_FORMAT_DELETE_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        filingHistoryApiClient.deleteFilingHistory(API_CLIENT_REQUEST);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).deleteFilingHistory(expectedUri, DELTA_AT, ENTITY_ID, PARENT_ENTITY_ID);
        verify(privateFilingHistoryDelete).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }

    @Test
    void shouldHandleURIValidationExceptionWhenSendingDeleteRequest() throws Exception {
        // given
        Class<URIValidationException> exceptionClass = URIValidationException.class;

        when(internalApiClientFactory.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteFilingHistory(anyString(), anyString(), anyString(), anyString())).thenReturn(
                privateFilingHistoryDelete);
        when(privateFilingHistoryDelete.execute()).thenThrow(exceptionClass);

        DataMapHolder.get().requestId(REQUEST_ID);
        final String expectedUri = PRE_FORMAT_DELETE_URI.formatted(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        filingHistoryApiClient.deleteFilingHistory(API_CLIENT_REQUEST);

        // then
        verify(apiClient).setRequestId(REQUEST_ID);
        verify(internalApiClient).privateDeltaResourceHandler();
        verify(privateDeltaResourceHandler).deleteFilingHistory(expectedUri, DELTA_AT, ENTITY_ID, PARENT_ENTITY_ID);
        verify(privateFilingHistoryDelete).execute();
        verify(responseHandler).handle(any(exceptionClass));
    }
}
