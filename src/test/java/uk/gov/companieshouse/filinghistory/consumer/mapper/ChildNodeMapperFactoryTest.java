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
    @Mock
    private AssociatedFilingNodeMapper associatedFilingNodeMapper;
    @Mock
    private EmbeddedChildNodeMapper embeddedChildNodeMapper;

    @Test
    void shouldReturnAnnotationNodeMapperWhenPassedAnnotationTransactionKindEnum() {
        // given

        // when
        ChildNodeMapper expected = childNodeMapperFactory.getChildMapper(TransactionKindEnum.ANNOTATION);

        // then
        assertInstanceOf(AnnotationNodeMapper.class, expected);
    }

    @Test
    void shouldReturnAssociatedFilingNodeMapperWhenPassedAssociatedFilingTransactionKindEnum() {
        // given

        // when
        ChildNodeMapper expected = childNodeMapperFactory.getChildMapper(TransactionKindEnum.ASSOCIATED_FILING);

        // then
        assertInstanceOf(AssociatedFilingNodeMapper.class, expected);
    }

    @Test
    void shouldReturnEmbeddedChildNodeMapperWhenPassedTopLevelTransactionKindEnum() {
        // given

        // when
        ChildNodeMapper expected = childNodeMapperFactory.getChildMapper(TransactionKindEnum.TOP_LEVEL);

        // then
        assertInstanceOf(EmbeddedChildNodeMapper.class, expected);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenPassedInvalidTransactionKindEnum() {
        // given

        // when
        Executable executable = () -> childNodeMapperFactory.getChildMapper(TransactionKindEnum.RESOLUTION);

        // then
        assertThrows(IllegalStateException.class, executable);
    }
}
