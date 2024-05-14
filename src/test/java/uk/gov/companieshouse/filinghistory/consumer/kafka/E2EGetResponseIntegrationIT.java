package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findFilingHistoryDocument;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getDeltaApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.web.client.RestClient;

@SuppressWarnings("unchecked")
class E2EGetResponseIntegrationIT {

    private final RestClient apiClient = getFilingHistoryApiRestClient();
    private final RestClient deltaApi = getDeltaApiRestClient();

    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL)
            .setSerializationInclusion(Include.NON_EMPTY)
            .registerModule(new JavaTimeModule());

    private final MongoClient mongoClient = BulkIntegrationTestUtils.mongoClient();
    private final MongoCollection<Document> filingHistoryCollection = getFilingHistoryCollection(mongoClient);

    @ParameterizedTest(name = "[{index}] {1}/{2}/{0}")
    @ArgumentsSource(IntegrationGETResponseGenerator.class)
    @EnabledIfEnvironmentVariable(disabledReason = "Disabled for normal builds", named = "RUN_BULK_TEST",
            matches = "^(1|true|TRUE)$")
    void shouldMatchGetResponsesFromPerlEndpoints(String transactionId, String formType, String entityId,
            String companyNumber, String delta, String perlGetSingleResponse, String perlGetListResponse)
            throws IOException {
        // Given

        Map<String, Object> perlSingleDocument = parseAndRemoveNulls(perlGetSingleResponse);
        Map<String, Object> perlDocumentList = parseAndRemoveNulls(perlGetListResponse);

        // When
        postDelta(delta, deltaApi);

        // Then
        Document document = null;
        try {
            document = findFilingHistoryDocument(filingHistoryCollection, formType, entityId, 20_000);
            assertNotNull(document);

            String singleDocumentJson = apiClient.get()
                    .uri("/company/{companyNumber}/filing-history/{transactionId}", companyNumber, transactionId)
                    .header("x-request-id", UUID.randomUUID().toString())
                    .retrieve()
                    .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                        throw new RuntimeException(response.getStatusText());
                    })
                    .body(String.class);
            String documentListJson = apiClient.get()
                    .uri("/company/{companyNumber}/filing-history", companyNumber)
                    .header("x-request-id", UUID.randomUUID().toString())
                    .retrieve()
                    .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                        throw new RuntimeException(response.getStatusText());
                    })
                    .body(String.class);

            Map<String, Object> singleDocument = mapper.readValue(singleDocumentJson, LinkedHashMap.class);
            Map<String, Object> documentList = mapper.readValue(documentListJson, LinkedHashMap.class);

            assertEquals(perlSingleDocument, singleDocument);
            assertEquals(perlDocumentList, documentList);
        } finally {
            if (document != null) {
                filingHistoryCollection.deleteOne(document);
            }
        }
    }

    private Map<String, Object> parseAndRemoveNulls(String json) throws JsonProcessingException {
        Map<String, Object> jsonNode = mapper.readValue(json, LinkedHashMap.class);
        stripEmpty(jsonNode);
        return jsonNode;
    }

    @SuppressWarnings("rawtypes")
    private void stripEmpty(Map<String, Object> map) {
        map.values().removeIf(Objects::isNull);
        map.forEach((key, value) -> {
            switch (value) {
                case Map m -> stripEmpty(m);
                case List l -> l.forEach(e -> {
                    if (e instanceof Map m) {
                        stripEmpty(m);
                    }
                });
                default -> {
                }
            }
        });
    }
}
