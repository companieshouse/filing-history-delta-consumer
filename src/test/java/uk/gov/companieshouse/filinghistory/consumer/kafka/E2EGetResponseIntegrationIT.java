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
        Document document = findFilingHistoryDocument(filingHistoryCollection, formType, entityId, 10_000);
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
            removeEmptyDescriptionValuesMissingOrEmptyInPerlFromJava(perlSingleDocument, javaSingleDocument);
            removeMismatchedActionDates(perlSingleDocument, javaSingleDocument);
            removeMismatchedAssociatedFilingsDescriptionValues(perlSingleDocument, javaSingleDocument);
            removeKnownMismatchedDescriptions(perlSingleDocument, javaSingleDocument);
        }
    }

    private void maskDocumentListKnownBugs(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        if (!perlDocumentList.equals(javaDocumentList)) {
            removeEmptyBarcodeDocumentList(perlDocumentList, javaDocumentList);
            removeEmptyItemsInDocumentList(javaDocumentList);
            removeEmptyDescriptionValuesInDocumentsList(perlDocumentList);
            removeMismatchedAssociatedFilingsDocumentList(perlDocumentList, javaDocumentList);
            removeEmptyDescriptionValuesMissingOrEmptyInPerlListFromJava(perlDocumentList, javaDocumentList);
            removeMismatchedAssociatedFilingsDescriptionValuesDocumentList(perlDocumentList, javaDocumentList);
            removeKnownMismatchedDescriptionsDocumentList(perlDocumentList, javaDocumentList);
        }
    }

    private void removeKnownMismatchedDescriptions(JsonNode perlNode, JsonNode javaNode) {
        if (!javaNode.isEmpty() && !perlNode.isEmpty()) {
            String javaDescription = javaNode.at(toJsonPtr("description")).textValue();
            String perlDescription = perlNode.at(toJsonPtr("description")).textValue();

            if (javaDescription != null && perlDescription != null
                    && javaDescription.equals(
                    "second-filing-change-to-a-person-with-significant-control-without-name-date")
                    && perlDescription.equals(
                    "second-filing-change-details-of-a-person-with-significant-control")) {
                logger.info("Masking known description differences for form type '%s'".formatted(
                        javaNode.at(toJsonPtr("type")).textValue()));
                ((ObjectNode) javaNode).remove("description");
                ((ObjectNode) perlNode).remove("description");
            }
        }
    }

    private void removeKnownMismatchedDescriptionsDocumentList(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        removeKnownMismatchedDescriptions(getFirstItem(perlDocumentList), getFirstItem(javaDocumentList));
    }

    private void removeMismatchedActionDates(JsonNode perlNode, JsonNode javaNode) {
        if (!javaNode.isEmpty() && !perlNode.isEmpty()) {
            if (!javaNode.at(toJsonPtr("associated_filings.0.action_date")).getNodeType()
                    .equals(perlNode.at(toJsonPtr("associated_filings.0.action_date")).getNodeType())) {
                logger.info("Masking mismatched types for 'associated_filings.0.action_date' values");
                ((ObjectNode) javaNode.at(toJsonPtr("associated_filings.0"))).remove("action_date");
                ((ObjectNode) perlNode.at(toJsonPtr("associated_filings.0"))).remove("action_date");
            }

            if (!javaNode.at(toJsonPtr("action_date")).getNodeType().equals(perlNode.at(toJsonPtr("action_date")))) {
                logger.info("Masking mismatched types for 'action_date' values");
                ((ObjectNode) javaNode).remove("action_date");
                ((ObjectNode) perlNode).remove("action_date");
            }
        }
    }

    private void removeMismatchedAssociatedFilingsDocumentList(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        removeMismatchedActionDates(getFirstItem(perlDocumentList), getFirstItem(javaDocumentList));
    }

    private void removeMismatchedAssociatedFilingsDescriptionValues(JsonNode perlSingleDocument,
            JsonNode javaSingleDocument) {
        if (javaSingleDocument != null && perlSingleDocument != null) {
            JsonNode jdv = javaSingleDocument.at(toJsonPtr("associated_filings.0.description_values"));
            JsonNode pdv = perlSingleDocument.at(toJsonPtr("associated_filings.0.description_values"));

            for (Iterator<String> it = jdv.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();

                if (!jdv.at(toJsonPtr(fieldName)).getNodeType().equals(pdv.at(toJsonPtr(fieldName)).getNodeType())) {
                    logger.info("Masking mismatched types for '%s' values".formatted(fieldName));
                    ((ObjectNode) jdv).remove(fieldName);
                    ((ObjectNode) pdv).remove(fieldName);
                }
            }
        }
    }

    private void removeMismatchedAssociatedFilingsDescriptionValuesDocumentList(JsonNode perlDocumentList,
            JsonNode javaDocumentList) {
        removeMismatchedAssociatedFilingsDescriptionValues(getFirstItem(perlDocumentList),
                getFirstItem(javaDocumentList));
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

    private void removeEmptyDescriptionValuesMissingOrEmptyInPerlFromJava(JsonNode perlSingleDocument,
            JsonNode javaSingleDocument) {
        JsonNode javaDescriptionValues = javaSingleDocument.at(toJsonPtr("description_values"));
        JsonNode perlDescriptionValues = perlSingleDocument.at(toJsonPtr("description_values"));

        for (Iterator<String> it = javaDescriptionValues.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            if (!perlDescriptionValues.has(fieldName) || perlDescriptionValues.get(fieldName).textValue()
                    .isBlank()) {
                ((ObjectNode) javaDescriptionValues).remove(fieldName);
            }
        }
    }

    private void removeEmptyDescriptionValuesMissingOrEmptyInPerlListFromJava(JsonNode perlDocumentList,
            JsonNode javaDocumentList) {
        removeEmptyDescriptionValuesMissingOrEmptyInPerlFromJava(perlDocumentList.at(toJsonPtr("items.0")),
                javaDocumentList.at(toJsonPtr("items.0")));
    }

    private void removeEmptyDescriptionValuesInDocumentsList(JsonNode jsonNode) {
        JsonNode items = jsonNode.at(toJsonPtr("items"));
        items.forEach(this::removeEmptyDescriptionValues);
    }

    private void removeEmptyBarcodeInResolutions(JsonNode perlDocument, JsonNode javaDocument) {

        if (perlDocument.has("resolutions")) {

            if (perlDocument.at(toJsonPtr("barcode")).isEmpty()) {
                logger.info("Masking root barcodes");
                ((ObjectNode) javaDocument).remove("barcode");
                ((ObjectNode) perlDocument).remove("barcode");
            }

            if (perlDocument.at(toJsonPtr("resolutions.0.barcode")).isEmpty()) {
                logger.info("Masking resolutions barcodes");
                ((ObjectNode) javaDocument.at(toJsonPtr("resolutions.0"))).remove("barcode");
                ((ObjectNode) perlDocument.at(toJsonPtr("resolutions.0"))).remove("barcode");
            }
        }
    }

    private void removeEmptyBarcodeDocumentList(JsonNode perlDocumentList, JsonNode javaDocumentList) {
        if (!javaDocumentList.at(toJsonPtr("items")).isEmpty()) {
            if (!javaDocumentList.at(toJsonPtr("items.0.resolutions")).isEmpty()) {
                ((ObjectNode) javaDocumentList.at(toJsonPtr("items.0.resolutions.0"))).remove("barcode");
                ((ObjectNode) perlDocumentList.at(toJsonPtr("items.0.resolutions.0"))).remove("barcode");
            }
            removeEmptyBarcodeInResolutions(perlDocumentList.at(toJsonPtr("items.0")),
                    javaDocumentList.at(toJsonPtr("items.0")));
        }
    }

    private JsonNode getFirstItem(JsonNode documentList) {
        return documentList.at(toJsonPtr("items.0"));
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
