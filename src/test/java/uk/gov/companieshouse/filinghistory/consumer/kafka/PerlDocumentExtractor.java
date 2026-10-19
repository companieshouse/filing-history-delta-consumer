package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findAllDeltas;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findFilingHistoryDocument;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getBackendRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getFilingHistoryCollection;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.getQueueApiRestClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.mongoClient;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.postDelta;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.sleep;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

public class PerlDocumentExtractor {

    private static final Logger logger = LoggerFactory.getLogger(PerlDocumentExtractor.class);
    private static final String BULK_TESTING_PERL_DOCS_CSV = "bulk-testing-perl-docs.csv";
    private static final String HEADER_ROW_FMT = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s";
    private static final String ROW_FMT =
            "%s"
                    + "\t%s"
                    + "\t%s"
                    + "\t%s"
                    + "\t%s"
                    + "\t%s"
                    + "\t%s"
                    + "\t%s"
                    + "\t%s";

    public static void main(String[] args) throws IOException {

        FileUtils.touch(new File(BULK_TESTING_PERL_DOCS_CSV));

        try (MongoClient mongoClient = mongoClient();
                PrintWriter out = new PrintWriter(BULK_TESTING_PERL_DOCS_CSV)) {

            out.println(HEADER_ROW_FMT.formatted("entity_id", "transaction_id", "form_type", "company_number",
                    "perl_delta", "java_delta", "perl_document", "perl_get_single_response", "perl_get_list_response"));

            MongoCollection<Document> filingHistoryCollection = getFilingHistoryCollection(mongoClient);
            RestClient queueApiClient = getQueueApiRestClient();
            RestClient backendClient = getBackendRestClient();

            filingHistoryCollection.deleteMany(new Document());

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

                        out.println(ROW_FMT.formatted(entityId, transactionId, formType,
                                companyNumber, escapeSpecialCharacters(deltas.perlDelta()),
                                escapeSpecialCharacters(deltas.javaDelta()),
                                escapeSpecialCharacters(perlDocument.toJson()),
                                escapeSpecialCharacters(perlGetSingleResponse),
                                escapeSpecialCharacters(perlGetListResponse)));

                    } else {
                        logger.warn("No delta JSON found for transaction {}", deltas.entity_id());
                    }
                } catch (Exception e) {
                    logger.error("Processing failed Perl delta: transaction_id %s, Perl %s".formatted(
                            deltas.entity_id(), deltas.perlDelta()), e);
                }
            });
            logger.info("Done");
        } catch (SQLException e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String escapeSpecialCharacters(String data) {
        Objects.requireNonNull(data, "Input data cannot be null");
        return data.replaceAll("\\R", "");
    }


    private static String fetchGetSingleDocument(String companyNumber, String transactionId, RestClient backendClient) {

        for (int count = 0; count < 2000; count++) {
            try {
                sleep(1);

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

    private static String fetchGetDocumentList(String companyNumber, RestClient backendClient) {

        for (int count = 0; count < 2000; count++) {
            try {
                sleep(1);

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
