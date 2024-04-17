package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@Component
public class ChildNodeMapperFactory {

    private final AnnotationNodeMapper annotationNodeMapper;
    private final AssociatedFilingNodeMapper associatedFilingNodeMapper;
    private final EmbeddedChildNodeMapper embeddedChildNodeMapper;

    public ChildNodeMapperFactory(AnnotationNodeMapper annotationNodeMapper,
                                  AssociatedFilingNodeMapper associatedFilingNodeMapper, EmbeddedChildNodeMapper embeddedChildNodeMapper) {
        this.annotationNodeMapper = annotationNodeMapper;
        this.associatedFilingNodeMapper = associatedFilingNodeMapper;
        this.embeddedChildNodeMapper = embeddedChildNodeMapper;
    }

    public ChildNodeMapper getChildMapper(TransactionKindEnum kind) {
        return switch (kind) {
            case ANNOTATION -> annotationNodeMapper;
            case ASSOCIATED_FILING -> associatedFilingNodeMapper;
            case TOP_LEVEL -> embeddedChildNodeMapper;
            default -> throw new IllegalStateException("Unexpected transaction kind: " + kind.getValue());
        };
    }
}
