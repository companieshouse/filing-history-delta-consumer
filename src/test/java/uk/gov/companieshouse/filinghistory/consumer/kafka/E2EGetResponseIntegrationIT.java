package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findFilingHistoryDocument;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getDeltaApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

class E2EGetResponseIntegrationIT {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RestClient apiClient = getFilingHistoryApiRestClient();
    private final RestClient deltaApi = getDeltaApiRestClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

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

            JsonNode javaSingleDocument = parseAndRemoveNulls(singleDocumentJson);
            JsonNode javaDocumentList = parseAndRemoveNulls(documentListJson);

            maskSingleDocumentKnownBugs(perlSingleDocument, javaSingleDocument);
            maskDocumentListKnownBugs(perlDocumentList, javaDocumentList);

            if (!perlSingleDocument.equals(javaSingleDocument)) {
                logger.error("Expected: {}", mapper.writeValueAsString(perlSingleDocument));
                logger.error("  Actual: {}", mapper.writeValueAsString(javaSingleDocument));
            }
            if (!perlDocumentList.equals(javaDocumentList)) {
                logger.error("Expected list: {}", perlGetListJson);
                logger.error("  Actual list: {}", documentListJson);
            }

            assertEquals(perlSingleDocument, javaSingleDocument);
            assertEquals(perlDocumentList, javaDocumentList);
        } finally {
            if (document != null) {
                filingHistoryCollection.deleteOne(document);
            }
        }
    }

    private void maskSingleDocumentKnownBugs(JsonNode perlSingleDocument, JsonNode javaSingleDocument) {
        if (!perlSingleDocument.equals(javaSingleDocument)) {
            removeEmptyBarcodeInResolutions(perlSingleDocument, javaSingleDocument);
            removeEmptyDescriptionValues(perlSingleDocument);
            removeEmptyDescriptionValuesMissingOrEmptyInPerl(perlSingleDocument, javaSingleDocument);
            removeActionDates(perlSingleDocument, javaSingleDocument);
        }
    }

    private void maskDocumentListKnownBugs(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        if (!perlDocumentList.equals(javaDocumentList)) {
            removeEmptyBarcodeDocumentList(perlDocumentList, javaDocumentList);
            removeEmptyItemsInDocumentList(javaDocumentList);
            removeEmptyDescriptionValuesInDocumentsList(perlDocumentList);
            removeEmptyActionDatesDocumentList(perlDocumentList, javaDocumentList);
            removeEmptyDescriptionValuesMissingOrEmptyInPerlList(perlDocumentList, javaDocumentList);
        }
    }

    private void removeActionDates(JsonNode perlNode, JsonNode javaNode) {
        logger.info("Masking root action_dates");
        if (javaNode != null) {
            ((ObjectNode) javaNode).remove("action_date");
        }
        if (perlNode != null) {
            ((ObjectNode) perlNode).remove("action_date");
        }
    }

    private void removeEmptyActionDatesDocumentList(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        removeActionDates(getFirstItem(javaDocumentList).orElse(null),
                getFirstItem(perlDocumentList).orElse(null));
    }

    private void removeEmptyDescriptionValues(JsonNode jsonNode) {
        logger.info("Masking empty description_values");
        if (jsonNode.has("description_values")) {
            JsonNode descriptionValues = jsonNode.get("description_values");
            for (Iterator<String> it = descriptionValues.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                if (descriptionValues.get(fieldName).textValue().isEmpty()) {
                    ((ObjectNode) descriptionValues).remove(fieldName);
                }
            }
            if (jsonNode.get("description_values").isEmpty()) {
                ((ObjectNode) jsonNode).remove("description_values");
            }
        }
    }

    private void removeEmptyDescriptionValuesMissingOrEmptyInPerl(JsonNode perlSingleDocument,
            JsonNode javaSingleDocument) {
        if (perlSingleDocument.has("description_values") && javaSingleDocument.has("description_values")) {
            JsonNode javaDescriptionValues = javaSingleDocument.get("description_values");
            JsonNode perlDescriptionValues = perlSingleDocument.get("description_values");

            for (Iterator<String> it = javaDescriptionValues.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                if (!perlDescriptionValues.has(fieldName)) {
                    ((ObjectNode) javaDescriptionValues).remove(fieldName);
                }
            }
        }
    }

    private void removeEmptyDescriptionValuesMissingOrEmptyInPerlList(JsonNode perlDocumentList,
            JsonNode javaDocumentList) {
        if (perlDocumentList.has("items") && javaDocumentList.has("items")) {
            removeEmptyDescriptionValuesMissingOrEmptyInPerl(perlDocumentList.get("items").get(0),
                    javaDocumentList.get("items").get(0));
        }
    }


    private void removeEmptyDescriptionValuesInDocumentsList(JsonNode jsonNode) {
        if (jsonNode.has("items")) {
            JsonNode items = jsonNode.get("items");
            items.forEach(this::removeEmptyDescriptionValues);
        }
    }

    private void removeEmptyBarcodeInResolutions(JsonNode perlDocument, JsonNode javaDocument) {

        if (perlDocument.has("resolutions")) {

            if (!perlDocument.has("barcode") || perlDocument.get("barcode").isEmpty()) {
                logger.info("Masking root barcodes");
                ((ObjectNode) javaDocument).remove("barcode");
                ((ObjectNode) perlDocument).remove("barcode");
            }

            if (perlDocument.get("resolutions").get(0).has("barcode")
                    && perlDocument.get("resolutions").get(0).get("barcode").isEmpty()) {
                logger.info("Masking resolutions barcodes");
                ((ObjectNode) javaDocument.get("resolutions").get(0)).remove("barcode");
                ((ObjectNode) perlDocument.get("resolutions").get(0)).remove("barcode");
            }
        }
    }

    private void removeEmptyBarcodeDocumentList(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        if (javaDocumentList.has("items") && !javaDocumentList.get("items").isEmpty()) {
            if (javaDocumentList.get("items").get(0).has("resolutions")) {
                ((ObjectNode) javaDocumentList.get("items").get(0).get("resolutions").get(0)).remove("barcode");
                ((ObjectNode) perlDocumentList.get("items").get(0).get("resolutions").get(0)).remove("barcode");
            }
            removeEmptyBarcodeInResolutions(javaDocumentList.get("items").get(0), perlDocumentList.get("items").get(0));
        }
    }

    private Optional<JsonNode> getFirstItem(JsonNode documentList) {
        if (documentList.has("items")) {
            JsonNode items = documentList.get("items");
            if (!items.isEmpty()) {
                return Optional.ofNullable(items.get(0));
            }
        }
        return Optional.empty();
    }

    private void removeEmptyItemsInDocumentList(JsonNode jsonNode) {
        if (jsonNode.has("items") && jsonNode.get("items").isEmpty()) {
            ((ObjectNode) jsonNode).remove("items");
        }
    }

    private JsonNode parseAndRemoveNulls(String json) throws JsonProcessingException {
        return mapper.readTree(
                mapper.writeValueAsString(
                        mapper.readValue(json, new TypeReference<LinkedHashMap<String, ?>>() {
                        })));
    }
}
