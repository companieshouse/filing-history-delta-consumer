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

public class PutRequestMatcher implements ValueMatcher<Request> {

    private final String expectedUrl;
    private final String expectedBody;

    public PutRequestMatcher(String expectedUrl, String expectedBody) {
        this.expectedUrl = expectedUrl;
        this.expectedBody = expectedBody;
    }

    @Override
    public MatchResult match(Request value) {
        return MatchResult.aggregate(
                matchUrl(value.getUrl()),
                matchMethod(value.getMethod()),
                matchBody(value.getBodyAsString()));
    }

    private MatchResult matchUrl(String actualUrl) {
        return MatchResult.of(expectedUrl.equals(actualUrl));
    }

    private MatchResult matchMethod(RequestMethod actualMethod) {
        return MatchResult.of(RequestMethod.PUT.equals(actualMethod));
    }

    private MatchResult matchBody(String actualBody) {
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(Include.NON_EMPTY)
                .registerModule(new JavaTimeModule());

        try {
            InternalFilingHistoryApi expected = mapper.readValue(expectedBody, InternalFilingHistoryApi.class);
            InternalFilingHistoryApi actual = mapper.readValue(actualBody, InternalFilingHistoryApi.class);
            return MatchResult.of(expected.equals(actual));
        } catch (JsonProcessingException ex) {
            return MatchResult.of(false);
        }
    }
}

