package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

public class BulkIntegrationTestUtils {

    private static final Logger logger = LoggerFactory.getLogger(BulkIntegrationTestUtils.class);

    private static final String KERMIT_CONNECTION = "jdbc:oracle:thin:KERMITUNIX2/%s@//chd-chipsdb:1521/chipsdev"
            .formatted(System.getenv("KERMIT_PASSWORD"));
    private static final String MONGO_CONNECTION = "mongodb://localhost:27017/";
    private static final String COMPANY_FILING_HISTORY = "company_filing_history";
    private static final String AUTH_HEADER = "Basic MkVUVTh3TkFtVWNyd096SFJpclJ5YzM1bVVrX1dGT21RYkhFMXNMcjo=";
    private static final String QUEUE_API_URL = "http://localhost:18201/queue/delta/filing-history";
    private static final String DELTA_API_URL = "http://api.chs.local:4001/delta/filing-history";
    private static final String BACKEND_API_URL = "http://api.chs.local:4001";
    private static final String FILING_HISTORY_API_URL = "http://api.chs.local:4001";

    private static final String FIND_ALL_DELTAS = """
            SELECT
                entity_id,
                queue_delta,
                api_delta
            FROM
                capdevjco2.fh_staging_deltas
            WHERE
                entity_id NOT IN (3168588719, 3183361204) -- Broken in Java and Perl consumers
            """;

    private static final String FIND_ALL_PERL_DOCS = """
            SELECT
                entity_id,
                transaction_id,
                form_type,
                company_number,
                perl_delta,
                java_delta,
                perl_document,
                perl_get_single_response,
                perl_get_list_response
            FROM
                capdevjco2.bulk_testing_perl_docs
            """;

    private BulkIntegrationTestUtils() {
    }

    static @Nonnull RestClient getQueueApiRestClient() {
        return getRestClient(QUEUE_API_URL);
    }

    static @Nonnull RestClient getBackendRestClient() {
        return getRestClient(BACKEND_API_URL);
    }

    static @Nonnull RestClient getDeltaApiRestClient() {
        return getRestClient(DELTA_API_URL);
    }

    static @Nonnull RestClient getFilingHistoryApiRestClient() {
        return getRestClient(FILING_HISTORY_API_URL);
    }

    static void postDelta(String delta, RestClient restClient) {
        restClient.post()
                .header("x-request-id", UUID.randomUUID().toString())
                .body(delta)
                .retrieve()
                .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                    throw new RuntimeException(response.getStatusText());
                });
    }

    static @Nonnull List<Deltas> findAllDeltas() throws SQLException {
        return new JdbcTemplate(chipsSource()).query(BulkIntegrationTestUtils.FIND_ALL_DELTAS,
                (rs, rowNum) -> new Deltas(
                        rs.getString("entity_id"),
                        rs.getString("api_delta"),
                        rs.getString("queue_delta")));
    }

    static @Nonnull List<PerlDocuments> findAllPerlDocs() throws SQLException {
        return new JdbcTemplate(chipsSource()).query(BulkIntegrationTestUtils.FIND_ALL_PERL_DOCS,
                (rs, rowNum) -> new PerlDocuments(
                        rs.getString("entity_id"),
                        rs.getString("transaction_id"),
                        rs.getString("form_type"),
                        rs.getString("company_number"),
                        rs.getString("perl_delta"),
                        rs.getString("java_delta"),
                        rs.getString("perl_document"),
                        rs.getString("perl_get_single_response"),
                        rs.getString("perl_get_list_response")));
    }

    static DataSource chipsSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(KERMIT_CONNECTION);
        return dataSource;
    }

    static MongoClient mongoClient() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .serverApi(serverApi)
                .applyConnectionString(new ConnectionString(MONGO_CONNECTION))
                .build();

        return MongoClients.create(settings);
    }

    static @Nonnull MongoCollection<Document> getFilingHistoryCollection(MongoClient mongoClient) {
        return mongoClient
                .getDatabase(COMPANY_FILING_HISTORY)
                .getCollection(COMPANY_FILING_HISTORY);
    }

    static Document findFilingHistoryDocument(MongoCollection<Document> collection, String formType, String entityId,
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

    private static @Nonnull RestClient getRestClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                .build();
    }

    public static void sleep(int timeout) {
        try {
            Thread.sleep(timeout); // nosonar
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}