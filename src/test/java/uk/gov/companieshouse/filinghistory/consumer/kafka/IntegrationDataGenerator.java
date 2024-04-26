package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.client.RestClient;

public class IntegrationDataGenerator implements ArgumentsProvider {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationDataGenerator.class);
    private static final String KERMIT_CONNECTION = "jdbc:oracle:thin:KERMITUNIX2/%s@//chd-chipsdb:1521/chipsdev"
            .formatted(System.getenv("KERMIT_PASSWORD"));
    private static final String MONGO_CONNECTION = "mongodb://localhost:27017/?retryWrites=false&loadBalanced=false&serverSelectionTimeoutMS=5000&connectTimeoutMS=10000";
    private static final String QUEUE_API_URL = "http://localhost:18201/queue/delta/filing-history";
    private static final String COMPANY_FILING_HISTORY = "company_filing_history";

    private static final String FIND_ALL_DELTAS = """
            SELECT
                transaction_id,
                 (
                     SELECT
                         pkg_chs_get_data.f_get_one_transaction(transaction_id, '29-OCT-21 14.20.43.360560000') AS result
                     FROM
                         dual
                 ) AS queue_delta,
                 (
                     SELECT
                         pkg_chs_get_data.f_get_one_transaction_api(transaction_id, '29-OCT-21 14.20.43.360560000') AS result
                     FROM
                         dual
                 ) AS api_delta
            FROM
                capdevjco2.fh_deltas
            --where transaction_id = 3066842559
            """;

    private record Deltas(String transactionId, String javaDelta, String perlDelta) {

    }

    static class DeltaRowMapper implements RowMapper<Deltas> {

        @Override
        public Deltas mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Deltas(
                    rs.getString("transaction_id"),
                    rs.getString("api_delta"),
                    rs.getString("queue_delta"));
        }
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(chipsSource());
            MongoClient mongoClient = mongoClient();
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, COMPANY_FILING_HISTORY);
            MongoCollection<Document> filingHistoryCollection = mongoTemplate.getCollection(COMPANY_FILING_HISTORY);
            RestClient queueApiClient = RestClient.builder()
                    .baseUrl(QUEUE_API_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .build();

            filingHistoryCollection.deleteMany(new Document());

            Queue<Arguments> queue = new ConcurrentLinkedQueue<>();
            jdbcTemplate.query(FIND_ALL_DELTAS, new DeltaRowMapper())
                    .forEach(deltas -> {

                        // TODO Remove delta clean up when DSND-2475 is deployed in CIDEV
                        String perlDelta = cleanWhitespaceInKeys(deltas.perlDelta());
                        String javaDelta = cleanWhitespaceInKeys(deltas.javaDelta());
                        try {
                            if (deltas.javaDelta() != null && deltas.perlDelta() != null) {

                                postDeltaToQueueApi(perlDelta, queueApiClient);

                                DocumentContext delta = JsonPath.parse(deltas.javaDelta());
                                String entityId = delta.read("$.filing_history[0].entity_id");
                                String formType = delta.read("$.filing_history[0].form_type");

                                Document perlDocument = findPerlDocument(filingHistoryCollection, formType, entityId);
                                String expectedRequestBody = transformToPutRequest(perlDocument, delta);

                                queue.add(Arguments.of(getCategory(perlDocument), formType, entityId, javaDelta,
                                        expectedRequestBody));

                            } else {
                                logger.warn("No delta JSON found for transaction {}", deltas.transactionId());
                            }
                        } catch (RuntimeException e) {
                            logger.error("Processing failed for delta: transaction_id {}, Java {}, Perl {}",
                                    deltas.transactionId, javaDelta, perlDelta);
                        }
                    });
            logger.info("Done");
            return queue.stream();
        } catch (SQLException e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        }
    }

    private void postDeltaToQueueApi(String delta, RestClient queueApiClient) {
        queueApiClient.post()
                .header("x-request-id", UUID.randomUUID().toString())
                .body(delta)
                .retrieve()
                .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                    throw new RuntimeException(response.getStatusText());
                });
    }

    private Document findPerlDocument(MongoCollection<Document> filingHistoryCollection, String formType,
            String entityId) {
        Document document = null;
        for (int count = 0; count < 500; count++) {
            try {
                Thread.sleep(10); // nosonar
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            document = filingHistoryCollection.find().first();
            if (document != null) {
                break;
            }
        }

        if (document != null) {
            filingHistoryCollection.deleteMany(Filters.and(Filters.eq("_id", document.get("_id"))));
            return document;
        }

        throw new RuntimeException("Failed to read filing history document for form %s, transaction %s"
                .formatted(formType, entityId));
    }

    private String cleanWhitespaceInKeys(String delta) {
        return StringUtils.isNotBlank(delta) ? delta
                .replaceAll("\"psc_name\\s\"", "\"psc_name\"")
                .replaceAll("\"property_acquired_date\\s\"", "\"property_acquired_date\"")
                .replaceAll("\"mortgage_satisfaction_date\\s\"", "\"mortgage_satisfaction_date\"")
                : delta;
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

    List<?> formatDatesInArray(ArrayList<?> arrayNode) {
        if (arrayNode.getFirst() instanceof Document document) {
            formatDates(document);
        }
        return arrayNode;
    }

    //    @SuppressWarnings("unchecked")
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

    public DataSource chipsSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(KERMIT_CONNECTION);
        return dataSource;
    }

    public MongoClient mongoClient() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .serverApi(serverApi)
                .applyConnectionString(new ConnectionString(MONGO_CONNECTION))
                .build();

        return MongoClients.create(settings);
    }
}
