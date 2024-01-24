package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import org.springframework.stereotype.Component;

@Component
public class TransformerFactory {

    private final AddressCase addressCase;
    private final BsonDate bsonDate;
    private final LowerCase lowerCase;
    private final SentenceCase sentenceCase;
    private final TitleCase titleCase;

    public TransformerFactory(AddressCase addressCase, BsonDate bsonDate, LowerCase lowerCase,
            SentenceCase sentenceCase, TitleCase titleCase) {
        this.addressCase = addressCase;
        this.bsonDate = bsonDate;
        this.lowerCase = lowerCase;
        this.sentenceCase = sentenceCase;
        this.titleCase = titleCase;
    }

    public Transformer mapTransformer(String function) {
        return switch (function) {
            case "address_case" -> addressCase;
            case "bson_date" -> bsonDate;
            case "lc" -> lowerCase;
            case "sentence_case" -> sentenceCase;
            case "title_case" -> titleCase;
            default -> throw new IllegalArgumentException("Unexpected function " + function);
        };
    }
}
