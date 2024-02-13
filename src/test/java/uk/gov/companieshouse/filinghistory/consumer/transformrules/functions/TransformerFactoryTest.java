package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class TransformerFactoryTest {

    private TransformerFactory factory;
    private final AddressCase addressCase = TransformerTestingUtils.getAddressCase();
    private final FormatDate formatDate = TransformerTestingUtils.getBsonDate();
    private final SentenceCase sentenceCase = TransformerTestingUtils.getSentenceCase();
    private final TitleCase titleCase = TransformerTestingUtils.getTitleCase();
    private final ReplaceProperty replaceProperty = TransformerTestingUtils.getReplaceProperty();
    private final ProcessCapital processCapital = TransformerTestingUtils.getProcessCapital();

    @BeforeEach
    void setUp() {
        factory = new TransformerFactory(addressCase, formatDate, sentenceCase, titleCase, replaceProperty,
                processCapital);
    }

    @Test
    void shouldReturnProcessCapital() {
        // given

        // when
        ProcessCapital actual = factory.getProcessCapital();

        // then
        assertInstanceOf(ProcessCapital.class, actual);
    }

    @Test
    void shouldReturnReplaceProperty() {
        // given

        // when
        ReplaceProperty actual = factory.getReplaceProperty();

        // then
        assertInstanceOf(ReplaceProperty.class, actual);
    }

    @Test
    void shouldThrowExceptionWhenNoFunctionFound() {
        // given

        // when
        Executable actual = () -> factory.mapTransformer("invalid_function");

        // then
        assertThrows(RuntimeException.class, actual);
    }

    @ParameterizedTest
    @MethodSource("transformTestArgs")
    void mapTransformer(String functionName, Class<Transformer> expectedClass) {
        // given

        // when
        Transformer actual = factory.mapTransformer(functionName);

        // then
        assertInstanceOf(expectedClass, actual);
    }

    private static Stream<Arguments> transformTestArgs() {
        return Stream.of(
                Arguments.of("address_case", AddressCase.class),
                Arguments.of("bson_date", FormatDate.class),
                Arguments.of("sentence_case", SentenceCase.class),
                Arguments.of("title_case", TitleCase.class));
    }
}