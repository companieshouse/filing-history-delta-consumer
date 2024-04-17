package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.filinghistory.consumer.serdes.ArrayNodeDeserialiser;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class DescriptionValuesMapperTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private ArrayNodeDeserialiser<AltCapitalDescriptionValue> altCapitalArrayNodeDeserialiser;
    @Mock
    private ArrayNodeDeserialiser<CapitalDescriptionValue> capitalArrayNodeDeserialiser;

    @Mock
    private ArrayNode capitalArray;
    @Mock
    private ArrayNode altCapitalArray;
    @Mock
    private CapitalDescriptionValue capitalDescriptionValue;
    @Mock
    private AltCapitalDescriptionValue altCapitalDescriptionValue;

    @BeforeEach
    void setUp() {
        descriptionValuesMapper = new DescriptionValuesMapper(altCapitalArrayNodeDeserialiser,
                capitalArrayNodeDeserialiser);
    }

    @Test
    void shouldMapDescriptionValuesObject() {
        // given
        final ObjectNode jsonNode = MAPPER.createObjectNode()
                .putObject("description_values")
                .put("appointment_date", "01/01/2010")
                .put("branch_number", "50")
                .put("brought_down_date", "02/02/2011")
                .put("case_end_date", "06/05/2013")
                .put("case_number", "123")
                .put("cessation_date", "05/06/2013")
                .put("change_address", "11 Test Lane")
                .put("change_date", "04/04/2013")
                .put("change_details", "5 Test St")
                .put("change_name", "John Tester")
                .put("change_type", "type")
                .put("charge_creation_date", "05/05/2014")
                .put("charge_number", "1")
                .put("close_date", "06/06/2015")
                .put("company_number", "12345678")
                .put("company_type", "LLP")
                .put("date", "07/07/2016")
                .put("default_address", "5 Default Road")
                .put("description", "description")
                .put("form_attached", "attached form")
                .put("form_type", "TM01")
                .put("incorporation_date", "08/08/2017")
                .put("made_up_date", "09/09/2018")
                .put("new_address", "6 New town Crescent")
                .put("new_date", "10/10/2019")
                .put("new_jurisdiction", "Cardiff")
                .put("notification_date", "11/11/2020")
                .put("officer_address", "201 Officer Drive")
                .put("officer_name", "John Doe")
                .put("old_address", "5 Old Kent Road")
                .put("old_jurisdiction", "London")
                .put("original_description", "original")
                .put("property_acquired_date", "12/12/2021")
                .put("psc_name", "Significant Person")
                .put("representative_details", "details representing")
                .put("termination_date", "31/01/2022")
                .put("withdrawal_date", "01/02/2023");

        jsonNode.putIfAbsent("alt_capital", altCapitalArray);
        jsonNode.putIfAbsent("capital", capitalArray);

        final DescriptionValues expected = new DescriptionValues()
                .altCapital(List.of(altCapitalDescriptionValue))
                .appointmentDate("01/01/2010")
                .branchNumber("50")
                .broughtDownDate("02/02/2011")
                .capital(List.of(capitalDescriptionValue))
                .caseEndDate("06/05/2013")
                .caseNumber("123")
                .cessationDate("05/06/2013")
                .changeAddress("11 Test Lane")
                .changeDate("04/04/2013")
                .changeDetails("5 Test St")
                .changeName("John Tester")
                .changeType("type")
                .chargeCreationDate("05/05/2014")
                .chargeNumber("1")
                .closeDate("06/06/2015")
                .companyNumber("12345678")
                .companyType("LLP")
                .date("07/07/2016")
                .defaultAddress("5 Default Road")
                .description("description")
                .formAttached("attached form")
                .formType("TM01")
                .incorporationDate("08/08/2017")
                .madeUpDate("09/09/2018")
                .newAddress("6 New town Crescent")
                .newDate("10/10/2019")
                .newJurisdiction("Cardiff")
                .notificationDate("11/11/2020")
                .officerAddress("201 Officer Drive")
                .officerName("John Doe")
                .oldAddress("5 Old Kent Road")
                .oldJurisdiction("London")
                .originalDescription("original")
                .propertyAcquiredDate("12/12/2021")
                .pscName("Significant Person")
                .representativeDetails("details representing")
                .terminationDate("31/01/2022")
                .withdrawalDate("01/02/2023");

        when(altCapitalArrayNodeDeserialiser.deserialise(any())).thenReturn(List.of(altCapitalDescriptionValue));
        when(capitalArrayNodeDeserialiser.deserialise(any())).thenReturn(List.of(capitalDescriptionValue));

        // when
        final DescriptionValues actual = descriptionValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
        verify(altCapitalArrayNodeDeserialiser).deserialise(altCapitalArray);
        verify(capitalArrayNodeDeserialiser).deserialise(capitalArray);
    }

    @Test
    void shouldMapDescriptionValuesObjectWhenFieldsAreEmpty() {
        // given
        final JsonNode jsonNode = MAPPER.createObjectNode();

        final DescriptionValues expected = new DescriptionValues();

        // when
        final DescriptionValues actual = descriptionValuesMapper.map(jsonNode);

        // then
        assertEquals(expected, actual);
        assertNull(jsonNode.get("description_values"));
    }

    @Test
    void shouldReturnNullIfJsonNodeIsNull() {
        // given

        // when
        final DescriptionValues actual = descriptionValuesMapper.map(null);

        // then
        assertNull(actual);
    }
}
