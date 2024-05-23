package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findAllDeltas;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findFilingHistoryDocument;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getQueueApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.mongoClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

public class IntegrationDataGenerator implements ArgumentsProvider {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationDataGenerator.class);

    private static final Set<String> EXCLUSIONS = Set.of(
            // These entity IDs are for when Java has the correct behaviour
            "2042077166",
            "104004155",
            "3024249751",
            "3043332581",
            "3112605554"
            // END Java has the correct behaviour
    );

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

        try (MongoClient mongoClient = mongoClient()) {
            MongoCollection<Document> filingHistoryCollection = getFilingHistoryCollection(mongoClient);
            RestClient queueApiClient = getQueueApiRestClient();

            filingHistoryCollection.deleteMany(new Document());

            Queue<Arguments> queue = new ConcurrentLinkedQueue<>();
            findAllDeltas().forEach(deltas -> {

                try {
                    if (deltas.javaDelta() != null && deltas.perlDelta() != null) {
                        DocumentContext delta = JsonPath.parse(deltas.javaDelta());
                        String entityId = delta.read("$.filing_history[0].entity_id");
                        String formType = delta.read("$.filing_history[0].form_type");

                        if (!EXCLUSIONS.contains(entityId)) {

                            postDelta(deltas.perlDelta(), queueApiClient);

                            Document perlDocument = findPerlDocument(filingHistoryCollection, formType, entityId);
                            String expectedRequestBody = transformToPutRequest(perlDocument, delta);

                            queue.add(Arguments.of(getCategory(perlDocument), formType, entityId, deltas.javaDelta(),
                                    expectedRequestBody));
                        }

                    } else {
                        logger.warn("No delta JSON found for transaction {}", deltas.entity_id());
                    }
                } catch (RuntimeException e) {
                    logger.error("Processing failed for delta: transaction_id {}, Java {}, Perl {}",
                            deltas.entity_id(), deltas.javaDelta(), deltas.perlDelta());
                }
            });
            logger.info("Done");
            return queue.stream();
        } catch (Exception e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        }
    }

    private Document findPerlDocument(MongoCollection<Document> filingHistoryCollection, String formType,
            String entityId) {
        Document document = findFilingHistoryDocument(filingHistoryCollection, formType, entityId, 20_000);
        filingHistoryCollection.deleteMany(Filters.and(Filters.eq("_id", document.get("_id"))));
        return document;
    }

    @SuppressWarnings("unchecked")
    private String transformToPutRequest(Document filingHistoryDocument, DocumentContext javaDelta) {
        Document putRequest = new Document();
        String encodedTransactionId = (String) filingHistoryDocument.get("_id");

        Map<String, Object> externalData = new LinkedHashMap<>((Map<String, Object>) filingHistoryDocument.get("data"));
        externalData.put("transaction_id", encodedTransactionId);

        if (filingHistoryDocument.containsKey("_barcode")) {
            externalData.put("barcode",
                    filingHistoryDocument.get("_barcode"));
        } else if (filingHistoryDocument.containsKey("barcode")) {
            externalData.put("barcode",
                    filingHistoryDocument.get("barcode"));
        }

        externalData.remove("pages");
        Document links = (Document) externalData.get("links");
        links.remove("document_meta_data");

        if (externalData.containsKey("resolutions")) {
            List<Document> resolutions = (List<Document>) externalData.get("resolutions");
            resolutions.forEach(doc -> doc.remove("delta_at"));
        }

        cleanseValuesMap(externalData, "description_values");

        Map<String, Object> internalData = new LinkedHashMap<>();
        filingHistoryDocument.forEach((key, value) -> {
            switch (key) {
                case "_id":
                case "_barcode":
                case "barcode":
                case "delta_at":
                case "data":
                    break;
                case "_entity_id":
                    internalData.put("entity_id", value);
                    break;
                case "_document_id":
                    internalData.put("document_id", value);
                    break;
                default:
                    internalData.put(key, value);
            }
        });

        internalData.put("updated_by", "context_id");
        internalData.putIfAbsent("parent_entity_id", "");
        internalData.put("delta_at", javaDelta.read("$.delta_at"));
        cleanseValuesMap(internalData, "original_values");
        if (externalData.containsKey("category") && "resolution".equals(externalData.get("category"))) {
            internalData.put("entity_id", javaDelta.read("$.filing_history[0].entity_id"));
            String documentId = javaDelta.read("$.filing_history[0].document_id");
            if (StringUtils.isNotBlank(documentId)) {
                internalData.put("document_id", documentId);
            }
        }

        putRequest.put("external_data", externalData);
        putRequest.put("internal_data", internalData);

        formatDates(putRequest);

        return putRequest.toJson(JsonWriterSettings.builder()
                .indent(true)
                .outputMode(JsonMode.SHELL)
                .build());
    }

    @SuppressWarnings({"unchecked"})
    private void cleanseValuesMap(Map<String, Object> data, String key) {
        if (data.containsKey(key)) {
            Map<String, Object> valuesMap = new HashMap<>((Map<String, Object>) data.get(key));

            if (valuesMap.isEmpty()) {
                data.remove(key);
            } else {
                Map<String, Object> cleansed = valuesMap.entrySet().stream()
                        .filter(entry -> entry.getValue() != null && StringUtils.isNotBlank(
                                entry.getValue().toString()))
                        .collect(Collectors.toMap(Map.Entry<String, Object>::getKey,
                                Map.Entry<String, Object>::getValue));
                if (!valuesMap.equals(cleansed)) {
                    if (cleansed.isEmpty()) {
                        data.remove(key);
                    } else {
                        data.replace(key, cleansed);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void formatDates(Map<String, Object> map) {
        map.forEach((key, value) -> {
            if (value != null) {
                switch (value) {
                    case Date date -> map.replace(key, date.toInstant().toString());
                    case Map m -> formatDates(m);
                    case ArrayList array -> map.replace(key, formatDatesInArray(array));
                    default -> {
                    }
                }
            }
        });
    }

    private List<?> formatDatesInArray(ArrayList<?> arrayNode) {
        if (arrayNode.getFirst() instanceof Document document) {
            formatDates(document);
        }
        return arrayNode;
    }

    private String getCategory(Document filingHistoryDocument) {
        Document data = (Document) filingHistoryDocument.get("data");
        String category = (String) ((Document) filingHistoryDocument.get("data")).get("category");
        if (category == null) {
            List<?> children = null;
            if (data.containsKey("annotations")) {
                children = (List<?>) data.get("annotations");
            } else if (data.containsKey("resolutions")) {
                children = (List<?>) data.get("resolutions");
            }

            if (children != null) {
                category = (String) ((Document) children.getFirst()).get("category");
            }
        }

        if (category == null) {
            category = "unknown";
        }

        return category;
    }
}
