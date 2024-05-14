package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findAllDeltas;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findFilingHistoryDocument;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getBackendRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getQueueApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.mongoClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import java.sql.SQLException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

public class IntegrationGETResponseGenerator implements ArgumentsProvider {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationGETResponseGenerator.class);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

        try (MongoClient mongoClient = mongoClient()) {
            MongoCollection<Document> filingHistoryCollection = getFilingHistoryCollection(mongoClient);
            RestClient queueApiClient = getQueueApiRestClient();
            RestClient backendClient = getBackendRestClient();

            filingHistoryCollection.deleteMany(new Document());

            Queue<Arguments> queue = new ConcurrentLinkedQueue<>();
            findAllDeltas().forEach(deltas -> {

                try {
                    if (deltas.javaDelta() != null && deltas.perlDelta() != null) {

                        DocumentContext delta = JsonPath.parse(deltas.javaDelta());
                        String entityId = delta.read("$.filing_history[0].entity_id");
                        String formType = delta.read("$.filing_history[0].form_type");
                        String companyNumber = delta.read("$.filing_history[0].company_number");

                        postDelta(deltas.perlDelta(), queueApiClient);
                        Document perlDocument = findFilingHistoryDocument(filingHistoryCollection, formType, entityId,
                                20_000);

                        String transactionId = perlDocument.getString("_id");
                        String perlGetSingleResponse = fetchGetSingleDocument(companyNumber, transactionId,
                                backendClient);
                        String perlGetListResponse = fetchGetDocumentList(companyNumber, backendClient);

                        filingHistoryCollection.deleteMany(
                                Filters.and(Filters.eq("_id", perlDocument.get("_id"))));

                        queue.add(Arguments.of(transactionId, formType, entityId, companyNumber, deltas.javaDelta(),
                                perlGetSingleResponse, perlGetListResponse));

                    } else {
                        logger.warn("No delta JSON found for transaction {}", deltas.entity_id());
                    }
                } catch (Exception e) {
                    logger.error("Processing failed Perl delta: transaction_id %s, Perl %s".formatted(
                            deltas.entity_id(), deltas.perlDelta()), e);
                }
            });
            logger.info("Done");
            return queue.stream();
        } catch (SQLException e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        }
    }

    private String fetchGetSingleDocument(String companyNumber, String transactionId, RestClient backendClient) {

        for (int count = 0; count < 2000; count++) {
            try {
                try {
                    Thread.sleep(1); // nosonar
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return backendClient.get()
                        .uri("/company/{companyNumber}/filing-history/{transactionId}", companyNumber, transactionId)
                        .header("x-request-id", UUID.randomUUID().toString())
                        .retrieve()
                        .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                            throw new RuntimeException(response.getStatusText());
                        })
                        .body(String.class);
            } catch (Exception e) {
                logger.info("Retrying GET single filing history document for transaction %s"
                        .formatted(transactionId));
            }
        }
        throw new RuntimeException("Failed to GET single filing history document for transaction %s"
                .formatted(transactionId));
    }

    private String fetchGetDocumentList(String companyNumber, RestClient backendClient) {

        for (int count = 0; count < 2000; count++) {
            try {
                try {
                    Thread.sleep(1); // nosonar
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return backendClient.get()
                        .uri("/company/{companyNumber}/filing-history", companyNumber)
                        .header("x-request-id", UUID.randomUUID().toString())
                        .retrieve()
                        .onStatus(status -> status.value() != HttpStatus.SC_OK, (request, response) -> {
                            throw new RuntimeException(response.getStatusText());
                        })
                        .body(String.class);
            } catch (Exception e) {
                logger.info("Retrying GET filing history list for company %s".formatted(companyNumber));
            }
        }
        throw new RuntimeException("Failed to GET filing history list for company %s".formatted(companyNumber));
    }
}
