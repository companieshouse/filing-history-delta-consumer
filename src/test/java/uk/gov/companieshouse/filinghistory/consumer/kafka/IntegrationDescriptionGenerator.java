package uk.gov.companieshouse.filinghistory.consumer.kafka;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class IntegrationDescriptionGenerator implements ArgumentsProvider {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationDataGenerator.class);
    private static final String KERMIT_CONNECTION = "jdbc:oracle:thin:KERMITUNIX2/%s@//chd-chipsdb:1521/chipsdev"
            .formatted(System.getenv("KERMIT_PASSWORD"));

    private static final String FIND_ALL_DESCRIPTIONS = """
            SELECT
                etd.entity_id   AS entity_id,
                etd.form_type   AS form_type,
                etd.description AS description,
                sd.api_delta    AS delta
            FROM
                     capdevjco2.fh_extracted_test_data etd
                JOIN capdevjco2.fh_staging_deltas sd ON etd.entity_id = sd.entity_id
            """;

    private record DeltaDescription(String entityId,
                                    String formType,
                                    String description,
                                    String delta) {

    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(chipsSource());

            Queue<Arguments> queue = new ConcurrentLinkedQueue<>();
            jdbcTemplate.query(FIND_ALL_DESCRIPTIONS, (rs, rowNum) -> new DeltaDescription(
                            rs.getString("entity_id"),
                            rs.getString("form_type"),
                            rs.getString("description").trim(),
                            rs.getString("delta")))
                    .forEach(deltaDescription -> {

                        if (deltaDescription.delta() != null) {

                            DocumentContext delta = JsonPath.parse(deltaDescription.delta());
                            String companyNumber = delta.read("$.filing_history[0].company_number");

                            queue.add(
                                    Arguments.of(deltaDescription.entityId(), deltaDescription.formType(),
                                            deltaDescription.description(), companyNumber, deltaDescription.delta()));

                        } else {
                            logger.warn("No delta JSON found for transaction {}", deltaDescription.entityId());
                        }
                    });
            logger.info("Done");
            return queue.stream();
        } catch (SQLException e) {
            logger.error("Failed accessing the CHIPS database", e);
            throw new RuntimeException(e);
        }
    }

    public DataSource chipsSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL(KERMIT_CONNECTION);
        return dataSource;
    }
}
