package uk.gov.companieshouse.filinghistory.consumer.datagenerator;

import static com.mongodb.client.model.Filters.eq;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestClient;

public class IntegrationDataGenerator implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationDataGenerator.class);
    private static final String KERMIT_CONNECTION = "jdbc:oracle:thin:KERMITUNIX2/%s@//chd-chipsdb:1521/chipsdev"
            .formatted(System.getenv("KERMIT_PASSWORD"));
    private static final String MONGO_CONNECTION = "mongodb://localhost:27017/?retryWrites=false&loadBalanced=false&serverSelectionTimeoutMS=5000&connectTimeoutMS=10000";
    private static final String QUEUE_API_URL = "http://localhost:18201/queue/delta/filing-history";
    private static final String COMPANY_FILING_HISTORY = "company_filing_history";
    private static final String PROJECT_ROOT = System.getenv()
            .getOrDefault("PROJECT_ROOT", "../filing-history-delta-consumer");
    private static final String TEST_DATA_CSV = "integration-test-data.csv";

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
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    record Deltas(String transactionId, String javaDelta, String perlDelta) {

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

    public static void main(String[] args) {
        new IntegrationDataGenerator().run();
    }

    @Override
    public void run() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(chipsSource());
            MongoClient mongoClient = mongoClient();
            MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, COMPANY_FILING_HISTORY);
            MongoCollection<Document> filingHistoryCollection = mongoTemplate.getCollection(COMPANY_FILING_HISTORY);
            RestClient queueApiClient = RestClient.builder()
                    .baseUrl(QUEUE_API_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .build();

            FileSystemUtils.deleteRecursively(getDataFolder());

            jdbcTemplate.query(FIND_ALL_DELTAS, new DeltaRowMapper())
                    .stream()
                    .parallel()
                    .forEach(deltas -> {
                        try {
                            if (deltas.javaDelta() != null && deltas.perlDelta() != null) {
                                postDeltaToQueueApi(deltas, queueApiClient);

                                DocumentContext documentContext = JsonPath.parse(deltas.javaDelta());
                                String entityId = documentContext.read("$.filing_history[0].entity_id");
                                String formType = documentContext.read("$.filing_history[0].form_type");
                                Document filingHistoryDocument = findFilingHistoryById(filingHistoryCollection,
                                        entityId);
                                String putRequest = transformToPutRequest(filingHistoryDocument, documentContext);

                                saveFiles(deltas.javaDelta(), putRequest, entityId, formType,
                                        getCategory(filingHistoryDocument));
                            } else {
                                logger.warn("No delta JSON found for transaction {}", deltas.transactionId());
                            }
                        } catch (RuntimeException e) {
                            logger.error("Processing failed for delta: {}", deltas);
                        }
                    });
            logger.info("Done");
        } catch (SQLException e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        }
    }

    private void postDeltaToQueueApi(Deltas deltas, RestClient queueApiClient) {
        queueApiClient.post()
                .header("x-request-id", UUID.randomUUID().toString())
                .body(deltas.perlDelta())
                .retrieve()
                .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                    throw new RuntimeException(response.getStatusText());
                });
    }

    @SuppressWarnings("unchecked")
    private String transformToPutRequest(Map<String, Object> filingHistoryDocument, DocumentContext javaDelta) {
        Document putRequest = new Document();
        String encodedTransactionId = encodeTransactionId((String) filingHistoryDocument.get("_entity_id"));

        Map<String, Object> externalData = new LinkedHashMap<>((Map<String, Object>) filingHistoryDocument.get("data"));
        externalData.put("transaction_id", encodedTransactionId);
        externalData.put("barcode", filingHistoryDocument.get("_barcode"));
        externalData.remove("pages");

        Map<String, Object> links = (Map<String, Object>) externalData.get("links");
        links.remove("document_meta_data");
        links.put("self", updateSelfLink((String) links.get("self"), encodedTransactionId));

        Map<String, Object> internalData = new LinkedHashMap<>();
        filingHistoryDocument.forEach((key, value) -> {
            switch (key) {
                case "_id":
                case "_barcode":
                case "delta_at":
                case "transaction_kind":
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
        internalData.put("transaction_kind", getTransactionKind(filingHistoryDocument));
        internalData.putIfAbsent("parent_entity_id", "");
        internalData.put("delta_at", javaDelta.read("$.delta_at"));

        putRequest.put("external_data", externalData);
        putRequest.put("internal_data", internalData);

        formatDates(putRequest);

        return putRequest.toJson(JsonWriterSettings.builder()
                .indent(true)
                .outputMode(JsonMode.SHELL)
                .build());
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

    private String updateSelfLink(String self, String encodedTransactionId) {
        int lastSlash = self.lastIndexOf("/");
        return self.substring(0, lastSlash + 1) + encodedTransactionId;
    }

    @SuppressWarnings("unchecked")
    private String getCategory(Map<String, Object> filingHistoryDocument) {
        return (String) ((Map<String, Object>) filingHistoryDocument.get("data")).get("category");
    }

    @SuppressWarnings("unchecked")
    private String getTransactionKind(Map<String, Object> filingHistoryDocument) {
        return switch ((String) ((Map<String, Object>) filingHistoryDocument.get("data")).get("type")) {
            case "annotation" -> "annotation";
            case "resolution" -> "resolution";
            case "associated-filing" -> "associated-filing";
            default -> "top-level";
        };
    }

    private String encodeTransactionId(String entityId) {
        return StringUtils.isBlank(entityId) ? entityId
                : Base64.encodeBase64URLSafeString((trim(entityId) + "salt").getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveFiles(String javaDelta, String putRequest, String entityId, String formType, String category) {
        formType = formType.replaceAll("/", "");

        File dataFolder = getDataFolder();

        String targetFolderName = "%s/%s/%s".formatted(category, formType, entityId);
        File targetFolder = new File(dataFolder, targetFolderName);
        targetFolder.mkdirs(); // nosonar

        try (
                FileWriter csvWriter = new FileWriter(new File(dataFolder, TEST_DATA_CSV), true);
                FileWriter deltaWriter = new FileWriter(
                        new File(targetFolder, "%s_delta.json".formatted(formType)));
                FileWriter putRequestWriter = new FileWriter(
                        new File(targetFolder, "%s_request_body.json".formatted(formType)))
        ) {
            Object jsonObject = objectMapper.readValue(javaDelta, Object.class);
            String prettyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObject);

            deltaWriter.write(prettyJson);
            putRequestWriter.write(putRequest);
            csvWriter.append(targetFolderName)
                    .append("/")
                    .append(formType)
                    .append("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private static File getDataFolder() {
        return new File("%s/target/generated-test-sources/data".formatted(PROJECT_ROOT));
    }

    private Document findFilingHistoryById(MongoCollection<Document> filingHistoryCollection, String entityId) {
        for (int count = 0; count < 500; count++) {
            try {
                Thread.sleep(10); // nosonar
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Document doc = filingHistoryCollection.find(eq("_entity_id", entityId)).first();
            if (doc != null) {
                return doc;
            }
        }
        throw new RuntimeException("Failed to read filing history document for transaction %s".formatted(entityId));
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
