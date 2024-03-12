package uk.gov.companieshouse.filinghistory.consumer.kafka;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;

public class DeleteRequestMatcher implements ValueMatcher<Request> {

    private final String expectedUrl;

    public DeleteRequestMatcher(String expectedUrl) {
        this.expectedUrl = expectedUrl;
    }

    @Override
    public MatchResult match(Request value) {
        return MatchResult.aggregate(
                matchUrl(value.getUrl()),
                matchMethod(value.getMethod()));
    }

    private MatchResult matchUrl(String actualUrl) {
        return MatchResult.of(expectedUrl.equals(actualUrl));
    }

    private MatchResult matchMethod(RequestMethod actualMethod) {
        return MatchResult.of(RequestMethod.DELETE.equals(actualMethod));
    }
}

