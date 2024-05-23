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
    private static final String FILING_HISTORY_API_URL = "http://api.chs.local:4001/filing-history-data-api";

    private static final String FIND_ALL_DELTAS = """
            SELECT
                entity_id,
                queue_delta,
                api_delta
            FROM
                (
                    SELECT
                        entity_id,
                        (
                            SELECT
                                pkg_chs_get_data.f_get_one_transaction(entity_id, '29-OCT-21 14.20.43.360560000')
                            FROM
                                dual
                        ) AS queue_delta,
                        (
                            SELECT
                                pkg_chs_get_data.f_get_one_transaction_api(entity_id, '29-OCT-21 14.20.43.360560000')
                            FROM
                                dual
                        ) AS api_delta
                    FROM
                        capdevjco2.fh_extracted_test_data
                    WHERE
                            loaded_into_chips_kermit = 'Y'
                        -- IDs broken in Perl and Java
                        AND entity_id NOT IN ( 3153600699, 3178873249, 3180140883, 3168588719, 3183442513,
                                               3181240723, 3181240912, 3182858493, 3183887704, 3246675970,
                                               3188166405, 3188752683, 3153598406, 3160750562, 3178873198,
                                               3157961596, 3183361204 )
                )
            WHERE
                queue_delta IS NOT NULL
            -- FETCH FIRST 600 ROWS ONLY
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
            try {
                Thread.sleep(1); // nosonar
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

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
}