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
                id,
                entity_id,
                form_type,
                description,
                (
                    SELECT
                        pkg_chs_get_data.f_get_one_transaction_api(entity_id, '29-OCT-21 14.20.43.360560000')
                    FROM
                        dual
                ) AS delta
            FROM
                capdevjco2.fh_extracted_test_data
            WHERE
                    loaded_into_chips_kermit = 'Y'
                AND id IS NOT NULL
                AND entity_id NOT IN ( 3153600699, 3168588719, 3178873249, 3180140883, 3183442513,
                                       3188166405, 3188752683, 3246675970, 3153598406, 3157961596,
                                       3160750562, 3178873198, 3181240723, 3181240912, 3182858493,
                                       3183361204, 3183887704 )
            """;

    private record DeltaDescription(String id,
                                    String entityId,
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
                            rs.getString("id"),
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
                                            deltaDescription.description(), deltaDescription.id(), companyNumber,
                                            deltaDescription.delta()));

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
