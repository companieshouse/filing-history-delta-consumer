package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@ExtendWith(MockitoExtension.class)
class ChildNodeMapperFactoryTest {

    @InjectMocks
    private ChildNodeMapperFactory childNodeMapperFactory;

    @Mock
    private AnnotationNodeMapper annotationNodeMapper;

    @Test
    void shouldReturnAnnotationNodeMapperWhenPassedAnnotationTransactionKindEnum() {
        // given

        // when
        ChildNodeMapper expected = childNodeMapperFactory.getChildMapper(TransactionKindEnum.ANNOTATION);

        // then
        assertInstanceOf(AnnotationNodeMapper.class, expected);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenPassedNonAnnotationTransactionKindEnum() {
        // given

        // when
        Executable executable = () -> childNodeMapperFactory.getChildMapper(TransactionKindEnum.TOP_LEVEL);

        // then
        assertThrows(IllegalStateException.class, executable);
    }
}
