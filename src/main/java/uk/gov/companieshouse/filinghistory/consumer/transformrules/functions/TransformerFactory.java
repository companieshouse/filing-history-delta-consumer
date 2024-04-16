package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import org.springframework.stereotype.Component;

@Component
public class TransformerFactory {

    private final AddressCase addressCase;
    private final AnnotationTransformer annotationTransformer;
    private final FormatDate formatDate;
    private final SentenceCase sentenceCase;
    private final TitleCase titleCase;
    private final ReplaceProperty replaceProperty;

    private final ProcessCapital processCapital;

    public TransformerFactory(AddressCase addressCase, AnnotationTransformer annotationTransformer, FormatDate formatDate,
                              SentenceCase sentenceCase, TitleCase titleCase, ReplaceProperty replaceProperty,
                              ProcessCapital processCapital) {
        this.addressCase = addressCase;
        this.annotationTransformer = annotationTransformer;
        this.formatDate = formatDate;
        this.sentenceCase = sentenceCase;
        this.titleCase = titleCase;
        this.replaceProperty = replaceProperty;
        this.processCapital = processCapital;
    }

    public Transformer mapTransformer(String function) {
        return switch (function) {
            case "address_case" -> addressCase;
            case "annotation" -> annotationTransformer;
            case "bson_date" -> formatDate;
            case "sentence_case" -> sentenceCase;
            case "title_case" -> titleCase;
            default -> throw new IllegalArgumentException("Unexpected function " + function);
        };
    }

    public ReplaceProperty getReplaceProperty() {
        return replaceProperty;
    }

    public ProcessCapital getProcessCapital() {
        return processCapital;
    }
}
