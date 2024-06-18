package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getDeltaApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.sleep;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        filingHistoryCollection.deleteMany(new Document());
    }

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

            String javaGetSingleJson = fetchSingleTransaction(transactionId, companyNumber, 1_000);
            String javaGetListJson = fetchTransactionList(companyNumber, 1_000);

            JsonNode javaSingleDocument = parseAndRemoveNulls(javaGetSingleJson);
            JsonNode javaDocumentList = parseAndRemoveNulls(javaGetListJson);

            maskSingleDocumentKnownBugs(perlSingleDocument, javaSingleDocument);
            maskDocumentListKnownBugs(perlDocumentList, javaDocumentList);

            if (!perlSingleDocument.equals(javaSingleDocument)) {
                logger.error("Expected: {}", perlGetSingleJson);
                logger.error("  Actual: {}", javaGetSingleJson);
            }
            if (!perlDocumentList.equals(javaDocumentList)) {
                logger.error("Expected list: {}", perlGetListJson);
                logger.error("  Actual list: {}", javaGetListJson);
            }

            assertEquals(perlSingleDocument, javaSingleDocument);
            assertEquals(perlDocumentList, javaDocumentList);
        } finally {
            if (document != null) {
                filingHistoryCollection.deleteOne(document);
            }
        }
    }

    private @Nullable String fetchTransactionList(String companyNumber, long waitMillis) {

        for (int count = 0; count < waitMillis; count++) {
            sleep(1);

            try {
                return apiClient.get()
                        .uri("/company/{companyNumber}/filing-history", companyNumber)
                        .header("x-request-id", UUID.randomUUID().toString())
                        .retrieve()
                        .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                            throw new RuntimeException(response.getStatusText());
                        })
                        .body(String.class);
            } catch (Exception e) {
                logger.info("Retrying to GET transaction list for {}", companyNumber);
            }
        }

        throw new RuntimeException("Retrying to GET transaction list for %s".formatted(companyNumber));
    }

    private @Nullable String fetchSingleTransaction(String transactionId, String companyNumber, long waitMillis) {
        for (int count = 0; count < waitMillis; count++) {
            sleep(1);

            try {
                return apiClient.get()
                        .uri("/company/{companyNumber}/filing-history/{transactionId}", companyNumber, transactionId)
                        .header("x-request-id", UUID.randomUUID().toString())
                        .retrieve()
                        .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                            throw new RuntimeException(response.getStatusText());
                        })
                        .body(String.class);
            } catch (Exception e) {
                logger.info("Retrying to GET single transaction for company {}, transaction {}", companyNumber,
                        transactionId, e);
            }
        }

        throw new RuntimeException("Retrying to GET transaction for company %s, transaction %s"
                .formatted(companyNumber, transactionId));
    }

    private Document findFilingHistoryDocument(MongoCollection<Document> collection, String formType, String entityId,
            long waitMillis) {
        for (int count = 0; count < waitMillis; count++) {
            sleep(1);

            Document document = collection.find().first();
            if (document != null) {
                logger.info("Document {} found after {} milliseconds", document.get("_id"), count);
                return document;
            }
        }

        throw new RuntimeException("Failed to read filing history document for form %s, entityId %s"
                .formatted(formType, entityId));
    }

    private void maskSingleDocumentKnownBugs(JsonNode perlSingleDocument, JsonNode javaSingleDocument) {
        if (!perlSingleDocument.equals(javaSingleDocument)) {
            removeEmptyBarcodeInResolutions(perlSingleDocument, javaSingleDocument);
            removeEmptyDescriptionValues(perlSingleDocument);
            removeEmptyDescriptionValuesMissingOrEmptyInPerl(perlSingleDocument, javaSingleDocument);
            removeMismatchedAssociatedFilingsActionDates(perlSingleDocument, javaSingleDocument);
            removeMismatchedAssociatedFilingsDescriptionValues(perlSingleDocument, javaSingleDocument);
        }
    }

    private void maskDocumentListKnownBugs(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        if (!perlDocumentList.equals(javaDocumentList)) {
            removeEmptyBarcodeDocumentList(perlDocumentList, javaDocumentList);
            removeEmptyItemsInDocumentList(javaDocumentList);
            removeEmptyDescriptionValuesInDocumentsList(perlDocumentList);
            removeMismatchedAssociatedFilingsDocumentList(perlDocumentList, javaDocumentList);
            removeEmptyDescriptionValuesMissingOrEmptyInPerlList(perlDocumentList, javaDocumentList);
            removeMismatchedAssociatedFilingsDescriptionValuesDocumentList(perlDocumentList, javaDocumentList);
        }
    }

    private void removeMismatchedAssociatedFilingsActionDates(JsonNode perlNode, JsonNode javaNode) {
        if (javaNode != null && perlNode != null) {
            JsonNode jad = javaNode.at(toJsonPtr("associated_filings.0.action_date"));
            JsonNode pad = perlNode.at(toJsonPtr("associated_filings.0.action_date"));

            if (!jad.getNodeType().equals(pad.getNodeType())) {
                logger.info("Masking mismatched types for 'associated_filings.0.action_date' values");
                ((ObjectNode) javaNode.at(toJsonPtr("associated_filings.0"))).remove("action_date");
                ((ObjectNode) perlNode.at(toJsonPtr("associated_filings.0"))).remove("action_date");
            }
        }
    }

    private void removeMismatchedAssociatedFilingsDocumentList(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        removeMismatchedAssociatedFilingsActionDates(getFirstItem(javaDocumentList).orElse(null),
                getFirstItem(perlDocumentList).orElse(null));
    }

    private void removeMismatchedAssociatedFilingsDescriptionValues(JsonNode perlSingleDocument,
            JsonNode javaSingleDocument) {
        if (javaSingleDocument != null && perlSingleDocument != null) {
            JsonNode jdv = javaSingleDocument.at(toJsonPtr("associated_filings.0.description_values"));
            JsonNode pdv = perlSingleDocument.at(toJsonPtr("associated_filings.0.description_values"));

            for (Iterator<String> it = jdv.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                JsonNode javaVal = jdv.at(toJsonPtr(fieldName));
                JsonNode perlVal = pdv.at(toJsonPtr(fieldName));

                if (!javaVal.getNodeType().equals(perlVal.getNodeType())) {
                    logger.info("Masking mismatched types for '%s' values".formatted(fieldName));
                    ((ObjectNode) jdv).remove(fieldName);
                    ((ObjectNode) pdv).remove(fieldName);
                }
            }
        }
    }

    private void removeMismatchedAssociatedFilingsDescriptionValuesDocumentList(JsonNode perlDocumentList,
            JsonNode javaDocumentList) {
        removeMismatchedAssociatedFilingsDescriptionValues(getFirstItem(javaDocumentList).orElse(null),
                getFirstItem(perlDocumentList).orElse(null));
    }

    private void removeEmptyDescriptionValues(JsonNode jsonNode) {
        logger.info("Masking empty description_values");
        JsonNode descriptionValues = jsonNode.at(toJsonPtr("description_values"));
        for (Iterator<String> it = descriptionValues.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            if (descriptionValues.get(fieldName).textValue().isEmpty()) {
                ((ObjectNode) descriptionValues).remove(fieldName);
            }
        }

        if (descriptionValues.isEmpty()) {
            ((ObjectNode) jsonNode).remove("description_values");
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
        JsonNode items = jsonNode.at(toJsonPtr("items"));
        items.forEach(this::removeEmptyDescriptionValues);
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
        JsonNode items = documentList.at(toJsonPtr("items"));
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    private void removeEmptyItemsInDocumentList(JsonNode jsonNode) {
        if (jsonNode.at(toJsonPtr("items")).isEmpty()) {
            ((ObjectNode) jsonNode).remove("items");
        }
    }

    private JsonNode parseAndRemoveNulls(String json) throws JsonProcessingException {
        return mapper.readTree(
                mapper.writeValueAsString(
                        mapper.readValue(json, new TypeReference<LinkedHashMap<String, ?>>() {
                        })));
    }

    public static JsonPointer toJsonPtr(String path) {
        return JsonPointer.compile("/%s".formatted(path.replace(".", "/")));
    }
}
