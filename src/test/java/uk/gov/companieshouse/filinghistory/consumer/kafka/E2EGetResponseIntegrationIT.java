package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findFilingHistoryDocument;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getDeltaApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.Iterator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

@SuppressWarnings("unchecked")
class E2EGetResponseIntegrationIT {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RestClient apiClient = getFilingHistoryApiRestClient();
    private final RestClient deltaApi = getDeltaApiRestClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final MongoClient mongoClient = BulkIntegrationTestUtils.mongoClient();
    private final MongoCollection<Document> filingHistoryCollection = getFilingHistoryCollection(mongoClient);

    @ParameterizedTest(name = "[{index}] {1}/{2}/{0}")
    @ArgumentsSource(IntegrationGETResponseGenerator.class)
    @EnabledIfEnvironmentVariable(disabledReason = "Disabled for normal builds", named = "RUN_BULK_TEST",
            matches = "^(1|true|TRUE)$")
    void shouldMatchGetResponsesFromPerlEndpoints(String transactionId, String formType, String entityId,
            String companyNumber, String delta, String perlGetSingleJson, String perlGetListJson)
            throws IOException {
        logger.info("Test case entityId {}, formType {}", entityId, formType);
        // Given

        JsonNode perlSingleDocument = parseAndRemoveNulls(perlGetSingleJson);
        JsonNode perlDocumentList = parseAndRemoveNulls(perlGetListJson);

        // When
        postDelta(delta, deltaApi);

        // Then
        Document document = null;
        try {
            document = findFilingHistoryDocument(filingHistoryCollection, formType, entityId, 10_000);
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

            JsonNode singleDocument = mapper.readTree(singleDocumentJson);
            JsonNode documentList = mapper.readTree(documentListJson);

            // Handle known bugs
            maskSingleDocumentKnownBugs(perlGetSingleJson, perlSingleDocument, singleDocument, singleDocumentJson);
            maskDocumentListKnownBugs(perlGetListJson, perlDocumentList, documentList, documentListJson);

            assertTrue(perlSingleDocument.equals(singleDocument));
            assertTrue(perlDocumentList.equals(documentList));
        } finally {
            if (document != null) {
                filingHistoryCollection.deleteOne(document);
            }
        }
    }

    private void maskDocumentListKnownBugs(String perlGetListJson, JsonNode perlDocumentList, JsonNode documentList,
            String documentListJson) {
        if (!perlDocumentList.equals(documentList)) {
            removeEmptyBarcodeInPerlDocumentList(perlDocumentList);
            removeEmptyItemsInDocumentList(documentList);
            removeEmptyDescriptionValuesInDocumentsList(perlDocumentList);

            // Failure not due to a known bug
            if (!perlDocumentList.equals(documentList)) {
                logger.error("Expected list: {}", perlGetListJson);
                logger.error("  Actual list: {}", documentListJson);
            }
        }
    }

    private void maskSingleDocumentKnownBugs(String perlGetSingleResponse, JsonNode perlSingleDocument,
            JsonNode singleDocument, String singleDocumentJson) {
        if (!perlSingleDocument.equals(singleDocument)) {
            removeEmptyBarcodeInPerl(perlSingleDocument);
            removeEmptyItemsList(singleDocument);
            removeEmptyDescriptionValues(perlSingleDocument);

            // Failure not due to a known bug
            if (!perlSingleDocument.equals(singleDocument)) {
                logger.error("Expected: {}", perlGetSingleResponse);
                logger.error("  Actual: {}", singleDocumentJson);
            }
        }
    }

    private void removeEmptyDescriptionValues(JsonNode jsonNode) {
        if (jsonNode.has("description_values")) {
            JsonNode descriptionValues = jsonNode.get("description_values");
            for (Iterator<String> it = descriptionValues.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                if (descriptionValues.get(fieldName).isEmpty()) {
                    ((ObjectNode) descriptionValues).remove(fieldName);
                }
            }
            ;
        }
    }

    private void removeEmptyDescriptionValuesInDocumentsList(JsonNode jsonNode) {
        if (jsonNode.has("items")) {
            JsonNode items = jsonNode.get("items");
            items.forEach(this::removeEmptyDescriptionValues);
        }
    }

    private void removeEmptyBarcodeInPerl(JsonNode jsonNode) {

        if (jsonNode.has("resolutions")) {
            if (jsonNode.has("barcode") && jsonNode.get("barcode").isEmpty()) {
                ((ObjectNode) jsonNode).remove("barcode");
            }

            JsonNode resolutions = jsonNode.get("resolutions");
            resolutions.forEach(resolution -> {
                if (resolution.has("barcode") && resolution.get("barcode").isEmpty()) {
                    ((ObjectNode) resolution).remove("barcode");
                }
            });
        }
    }

    private void removeEmptyBarcodeInPerlDocumentList(JsonNode jsonNode) {
        if (jsonNode.has("items")) {
            JsonNode items = jsonNode.get("items");
            items.forEach(this::removeEmptyBarcodeInPerl);
        }
    }

    private void removeEmptyItemsInDocumentList(JsonNode jsonNode) {
        if (jsonNode.has("items") && jsonNode.get("items").isEmpty()) {
            ((ObjectNode) jsonNode).remove("items");
        }
    }

    private void removeEmptyItemsList(JsonNode jsonNode) {
        if (jsonNode.has("items") && jsonNode.get("items").isEmpty()) {
            ((ObjectNode) jsonNode).remove("items");
        }
    }

    private JsonNode parseAndRemoveNulls(String json) throws JsonProcessingException {
        Map<String, Object> jsonNode = mapper.readValue(json, LinkedHashMap.class);
        stripEmpty(jsonNode);
        return mapper.valueToTree(jsonNode);
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
