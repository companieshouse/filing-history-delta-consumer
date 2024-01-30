package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BsonDateTest {

    private BsonDate bsonDate;

    @BeforeEach
    void beforeEach() {
        bsonDate = new BsonDate();
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("bsonDateFormatting")
    @DisplayName("Format text as a bson date")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        //given

        //when
        String output = bsonDate.transformBsonDate(input);

        //then
        assertEquals(expected, output);
    }

    //TODO check with test data whether or not we ever receive milliseconds in the delta in test data.
    private static Stream<Arguments> bsonDateFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("31/12/2021", "2021-12-31T00:00:00Z"),
                Arguments.of("20201014221720", "2020-10-14T22:17:20Z"),
                Arguments.of("20211231", "2021-12-31T00:00:00Z")
        );
    }

}
