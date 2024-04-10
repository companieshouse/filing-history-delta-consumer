package uk.gov.companieshouse.filinghistory.consumer.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@Component
public class ChildNodeMapperFactory {

    private final AnnotationNodeMapper annotationNodeMapper;
    private final AssociatedFilingNodeMapper associatedFilingNodeMapper;

    public ChildNodeMapperFactory(AnnotationNodeMapper annotationNodeMapper,
                                  AssociatedFilingNodeMapper associatedFilingNodeMapper) {
        this.annotationNodeMapper = annotationNodeMapper;
        this.associatedFilingNodeMapper = associatedFilingNodeMapper;
    }

    public ChildNodeMapper getChildMapper(TransactionKindEnum kind) {
        return switch (kind) {
            case ANNOTATION -> annotationNodeMapper;
            case ASSOCIATED_FILING -> associatedFilingNodeMapper;
            default -> throw new IllegalStateException("Unexpected value: " + kind.getValue());
        };
    }
}
