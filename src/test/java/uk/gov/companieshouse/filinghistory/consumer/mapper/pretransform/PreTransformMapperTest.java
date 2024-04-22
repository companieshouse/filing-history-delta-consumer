package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.ANNOTATION;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.ChildProperties;
import uk.gov.companieshouse.api.delta.FilingHistory;

@ExtendWith(MockitoExtension.class)
class PreTransformMapperTest {

    @InjectMocks
    private PreTransformMapper preTransformMapper;
    @Mock
    private DeltaNodeMapper deltaNodeMapper;
    @Mock
    private ChildNodeMapperFactory childNodeMapperFactory;

    @Mock
    private ObjectNode parentNode;
    @Mock
    private AnnotationNodeMapper annotationNodeMapper;
    @Mock
    private EmbeddedChildNodeMapper embeddedChildNodeMapper;
    @Mock
    private ObjectNode parentNodeWithChild;

    @Test
    void shouldMapTopLevelDeltaOntoObjectNode() {
        // given
        FilingHistory delta = new FilingHistory();

        when(deltaNodeMapper.mapToObjectNode(any())).thenReturn(parentNode);

        // when
        ObjectNode actual = preTransformMapper.mapDeltaToObjectNode(TOP_LEVEL, delta);

        // then
        assertEquals(parentNode, actual);
        verify(deltaNodeMapper).mapToObjectNode(delta);
        verifyNoInteractions(childNodeMapperFactory);
    }

    @Test
    void shouldMapAnnotationDeltaOntoObjectNode() {
        // given
        FilingHistory delta = new FilingHistory();

        when(deltaNodeMapper.mapToObjectNode(any())).thenReturn(parentNode);
        when(childNodeMapperFactory.getChildMapper(any())).thenReturn(annotationNodeMapper);
        when(annotationNodeMapper.mapChildObjectNode(any(), any())).thenReturn(parentNodeWithChild);

        // when
        ObjectNode actual = preTransformMapper.mapDeltaToObjectNode(ANNOTATION, delta);

        // then
        assertEquals(parentNodeWithChild, actual);
        verify(deltaNodeMapper).mapToObjectNode(delta);
        verify(childNodeMapperFactory).getChildMapper(ANNOTATION);
        verify(annotationNodeMapper).mapChildObjectNode(delta, parentNode);
    }

    @Test
    void shouldMapTopLevelDeltaWithEmbeddedChildOntoObjectNode() {
        // given
        FilingHistory delta = new FilingHistory()
                .child(List.of(new ChildProperties()));

        when(deltaNodeMapper.mapToObjectNode(any())).thenReturn(parentNode);
        when(childNodeMapperFactory.getChildMapper(any())).thenReturn(embeddedChildNodeMapper);
        when(embeddedChildNodeMapper.mapChildObjectNode(any(), any())).thenReturn(parentNodeWithChild);

        // when
        ObjectNode actual = preTransformMapper.mapDeltaToObjectNode(TOP_LEVEL, delta);

        // then
        assertEquals(parentNodeWithChild, actual);
        verify(deltaNodeMapper).mapToObjectNode(delta);
        verify(childNodeMapperFactory).getChildMapper(TOP_LEVEL);
        verify(embeddedChildNodeMapper).mapChildObjectNode(delta, parentNode);
    }
}
