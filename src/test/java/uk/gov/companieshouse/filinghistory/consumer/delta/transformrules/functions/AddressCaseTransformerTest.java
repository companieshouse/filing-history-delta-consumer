package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AddressCaseTransformerTest {

    private AddressCase titleCase;

    @BeforeEach
    void beforeEach() {
        titleCase = new AddressCase();
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("bsonDateFormatting")
    @DisplayName("Format text as a bson date")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        //given

        //when
        String output = titleCase.transformAddressCase(input);

        //then
        assertEquals(expected, output);
    }

    //TODO check with test data whether or not we ever receive milliseconds in the delta in test data.
    private static Stream<Arguments> bsonDateFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
//                Arguments.of("SAIL address changed from: THAMES HOUSE PORTSMOUTH ROAD ESHER SURREY KT10 9AD UNITED KINGDOM", ""),
//                Arguments.of("SEC APPOINTED        01/06/05  RUSSELL  NICHOLAS DAVID  Service address  LONDON  EC4A", ""),
                Arguments.of("ANALYST HOUSE 20-26 PEEL ROAD, DOUGLAS, IM99 1AP, ISLE OF MAN", "Analyst House 20-26 Peel Road, Douglas, IM99 1AP, Isle of Man"),
                Arguments.of("15 CANADA SQUARE LONDON E14 5GL", "15 Canada Square London E14 5GL"),
                Arguments.of("PO BOX 41 NORTH HARBOUR PORTSMOUTH HAMPSHIRE PO6 3AU", "PO Box 41 North Harbour Portsmouth Hampshire PO6 3AU"),
//                Arguments.of("", ""),
                Arguments.of("", "")
        );
    }

}
