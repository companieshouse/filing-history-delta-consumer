package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class AddressCaseTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private static final TitleCase titleCase = new TitleCase(MAPPER);

    private AddressCase addressCase;

    @BeforeEach
    void beforeEach() {
        addressCase = new AddressCase(MAPPER, titleCase);
    }

    @Test
    void shouldTransformCaptureGroupValueToAddressCaseAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .putObject("description_values")
                .put("representative_details", "Business Change Null");
        // when
        addressCase.transform(source, actual, "data.description_values.representative_details",
                List.of("personDetails"),
                Map.of("personDetails", "BUSINESS CHANGE null"));

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldTransformSourceNodeValueToAddressCaseAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        source.putObject("data")
                .put("description", "description of a person's DETAILS");
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .put("description", "description of a person's DETAILS")
                .putObject("description_values")
                .put("representative_details", "Description of a Person's Details");
        // when
        addressCase.transform(source, actual, "data.description_values.representative_details",
                List.of("data.description"),
                Map.of());

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("addressCaseFormatting")
    @DisplayName("Format text as a address case")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        // given

        // when
        String output = addressCase.transformAddressCase(input);

        // then
        assertEquals(expected, output);
    }

    private static Stream<Arguments> addressCaseFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("ANALYST HOUSE 20-26 PEEL ROAD, DOUGLAS, IM99 1AP, ISLE OF MAN",
                        "Analyst House 20-26 Peel Road, Douglas, IM99 1AP, Isle of Man"),
                Arguments.of("15 CANADA SQUARE LONDON E14 5GL", "15 Canada Square London E14 5GL"),
                Arguments.of("PO BOX 41 NORTH HARBOUR PORTSMOUTH HAMPSHIRE PO6 3AU",
                        "PO Box 41 North Harbour Portsmouth Hampshire PO6 3AU"),
                Arguments.of(
                        "NI636655: COMPANIES HOUSE DEFAULT ADDRESS, 2ND FLOOR THE LINENHALL 32-38 LINENHALL STREET, BELFAST, BT2 8BG",
                        "Ni636655: Companies House Default Address, 2nd Floor the Linenhall 32-38 Linenhall Street, Belfast, BT2 8BG")
        );
    }

}
