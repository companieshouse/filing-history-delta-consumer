package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class OriginalValuesMapperTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    @InjectMocks
    private OriginalValuesMapper originalValuesMapper;

    @Test
    void shouldMapOriginalValuesFromJsonNode() {
        // given
        final JsonNode jsonNode = MAPPER.createObjectNode()
                .putObject("original_values")
                .put("acc_type", "small")
                .put("accounting_period", "10 days")
                .put("action", "action")
                .put("appointment_date", "01/01/2010")
                .put("capital_type", "statement")
                .put("case_start_date", "04/07/2011")
                .put("case_end_date", "06/05/2013")
                .put("cessation_date", "05/06/2013")
                .put("change_date", "04/04/2013")
                .put("charge_creation_date", "05/05/2014")
                .put("made_up_date", "09/09/2018")
                .put("mortgage_satisfaction_date", "20/10/2005")
                .put("new_ro_address", "5 Test Road")
                .put("new_date", "10/10/2019")
                .put("notification_date", "11/11/2020")
                .put("officer_name", "John Doe")
                .put("period_type", "weeks")
                .put("property_acquired_date", "12/12/2021")
                .put("psc_name", "Significant Person")
                .put("resignation_date", "03/02/2013");

        final InternalDataOriginalValues expected = new InternalDataOriginalValues()
                .accType("small")
                .accountingPeriod("10 days")
                .action("action")
                .appointmentDate("01/01/2010")
                .capitalType("statement")
                .caseStartDate("04/07/2011")
                .caseEndDate("06/05/2013")
                .cessationDate("05/06/2013")
                .changeDate("04/04/2013")
                .chargeCreationDate("05/05/2014")
                .madeUpDate("09/09/2018")
                .mortgageSatisfactionDate("20/10/2005")
                .newRoAddress("5 Test Road")
                .newDate("10/10/2019")
                .notificationDate("11/11/2020")
                .officerName("John Doe")
                .periodType("weeks")
                .propertyAcquiredDate("12/12/2021")
                .pscName("Significant Person")
                .resignationDate("03/02/2013");

        // when
        final InternalDataOriginalValues actual = originalValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapNullValuesIfJsonNodeFieldsAreNull() {
        // given
        final JsonNode jsonNode = MAPPER.createObjectNode();

        // when
        final InternalDataOriginalValues actual = originalValuesMapper.map(jsonNode);

        // then
        assertNull(actual);
    }

    @Test
    void shouldReturnNullIfJsonNodeIsNull() {
        // given

        // when
        final InternalDataOriginalValues actual = originalValuesMapper.map(null);

        // then
        assertNull(actual);
    }
}
