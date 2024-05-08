package uk.gov.companieshouse.filinghistory.consumer.kafka;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public class PutRequestDescriptionMatcher implements ValueMatcher<Request> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(Include.NON_EMPTY)
            .registerModule(new JavaTimeModule());

    private final String expectedUrl;
    private final String expectedDescription;

    public PutRequestDescriptionMatcher(String expectedUrl, String expectedDescription) {
        this.expectedUrl = expectedUrl;
        this.expectedDescription = expectedDescription;
    }

    @Override
    public MatchResult match(Request value) {
        return MatchResult.aggregate(
                matchUrl(value.getUrl()),
                matchMethod(value.getMethod()),
                matchBody(value.getBodyAsString())
        );
    }

    private MatchResult matchUrl(String actualUrl) {
        return MatchResult.of(expectedUrl.equals(actualUrl));
    }

    private MatchResult matchMethod(RequestMethod actualMethod) {
        return MatchResult.of(RequestMethod.PUT.equals(actualMethod));
    }

    private MatchResult matchBody(String actualBody) {

        try {
            InternalFilingHistoryApi actual = MAPPER.readValue(actualBody, InternalFilingHistoryApi.class);
            String actualDescription = actual.getExternalData().getDescription();

            MatchResult result = MatchResult.of(expectedDescription.equals(actualDescription));
            if (!result.isExactMatch()) {
                System.out.printf("%nExpected description: [%s]%n", expectedDescription);
                System.out.printf("%nActual description:   [%s]", actualDescription);
            }
            return result;
        } catch (JsonProcessingException ex) {
            return MatchResult.of(false);
        }
    }
}

