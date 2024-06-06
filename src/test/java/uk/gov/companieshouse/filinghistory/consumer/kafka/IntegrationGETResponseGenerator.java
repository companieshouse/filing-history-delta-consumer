package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static uk.gov.companieshouse.filinghistory.consumer.kafka.BulkIntegrationTestUtils.findAllPerlDocs;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationGETResponseGenerator implements ArgumentsProvider {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationGETResponseGenerator.class);

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

        try {
            Queue<Arguments> queue = new ConcurrentLinkedQueue<>();
            findAllPerlDocs().forEach(docs -> {
                if (docs.javaDelta() != null && docs.perlDelta() != null) {
                    queue.add(Arguments.of(docs.transactionId(), docs.formType(), docs.entityId(), docs.companyNumber(),
                            docs.javaDelta(), docs.perlGetSingleResponse(), docs.perlGetListResponse()));
                } else {
                    logger.warn("No delta JSON found for transaction {}", docs.entityId());
                }
            });
            logger.info("Done");
            return queue.stream();
        } catch (SQLException e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        }
    }
}
