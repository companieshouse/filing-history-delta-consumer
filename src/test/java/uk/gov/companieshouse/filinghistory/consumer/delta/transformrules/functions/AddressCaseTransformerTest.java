package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AddressCaseTransformerTest {

    private AddressCase addressCase;

    @BeforeEach
    void beforeEach() {
        addressCase = new AddressCase();
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("addressCaseFormatting")
    @DisplayName("Format text as a address case")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        //given

        //when
        String output = addressCase.transformAddressCase(input);

        //then
        assertEquals(expected, output);
    }

    //TODO check with test data whether or not we ever receive milliseconds in the delta in test data.
    private static Stream<Arguments> addressCaseFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("ANALYST HOUSE 20-26 PEEL ROAD, DOUGLAS, IM99 1AP, ISLE OF MAN", "Analyst House 20-26 Peel Road, Douglas, IM99 1AP, Isle of Man"),
                Arguments.of("15 CANADA SQUARE LONDON E14 5GL", "15 Canada Square London E14 5GL"),
                Arguments.of("PO BOX 41 NORTH HARBOUR PORTSMOUTH HAMPSHIRE PO6 3AU", "PO Box 41 North Harbour Portsmouth Hampshire PO6 3AU"),
                Arguments.of("NI636655: COMPANIES HOUSE DEFAULT ADDRESS, 2ND FLOOR THE LINENHALL 32-38 LINENHALL STREET, BELFAST, BT2 8BG", "Ni636655: Companies House Default Address, 2nd Floor the Linenhall 32-38 Linenhall Street, Belfast, BT2 8BG")
        );
    }

}
