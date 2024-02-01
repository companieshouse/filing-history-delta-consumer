package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerUtils.toJsonPtr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SentenceCase extends AbstractTransformer {

    private static final Set<String> ENTITIES = new HashSet<>(Arrays.asList("ARD", "NI", "SE",
            "GB", "SC", "UK", "LTD", "L.T.D", "PLC", "P.L.C", "UNLTD", "CIC", "C.I.C", "LLP",
            "L.P", "LP", "EEIG", "OEIC", "ICVC", "AEIE", "C.B.C", "C.C.C", "CBC", "CBCN", "CBP",
            "CCC", "CYF", "EESV", "EOFG", "EOOS", "GEIE", "GELE", "PAC", "PCCLIMITED", "PCCLTD",
            "PROTECTEDCELLCOMPANY", "CWMNICELLGWARCHODEDIG", "CCGCYFYNGEDIG", "CCGCYF"));
    private static final Pattern FORWARDSLASH_ABBREVIATION = Pattern.compile("^(.?/)(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOKENISATION_PATTERN = Pattern.compile("(\\S+(\\s+|$))");
    private static final Pattern NEWLINE = Pattern.compile("\\n");
    private static final Pattern ABBREVIATIONS = Pattern.compile("\\b(\\p{L})[.]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern MIXED_ALPHANUMERIC = Pattern.compile("(\\w+\\d+\\w*|\\d+\\w+)");
    private static final Pattern MATCHES_ENTITY = Pattern.compile(
            "(\\b(?i:" + String.join("|", ENTITIES) + ")\\b)");

    //    private static final Pattern MATCHES_ENDING = Pattern.compile("(\\b(?i:' . join('|',reverse sort @endings) . ')\\b)")
    private static final Pattern OPENING_BRACKET = Pattern.compile("[(\\[]");
    private static final Pattern SENTENCE_TERMINATOR = Pattern.compile("[.!?]");
    private static final Pattern FIRST_LETTER = Pattern.compile("([a-z])",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_BRACKET = Pattern.compile("[])]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern GENERAL_ABBREVIATION = Pattern.compile(
            String.join("|",
                    "^(etc[.]|pp[.]|ph[.]?d[.]|",
                    "(?:[A-Z][.])(?:[A-Z][.])+|",
                    "(^[^a-zA-Z]*([a-z][.])+))[^a-z]*\\s"),
            Pattern.CASE_INSENSITIVE);
    static final Possessiveness NON_POSSESSIVE = new Possessiveness();

    public SentenceCase(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {

        String nodeText = target.objectNode().at(toJsonPtr(arguments.getFirst())).textValue();
        target.objectNode().put(target.field(), transformSentenceCase(nodeText));
    }

    String transformSentenceCase(String nodeText) {
        if (StringUtils.isEmpty(nodeText)) {
            return nodeText;
        }
        nodeText = nodeText.toUpperCase(Locale.UK);
        Matcher forwardslashAbbreviationMatcher = FORWARDSLASH_ABBREVIATION.matcher(nodeText);
        String start = "";
        if (forwardslashAbbreviationMatcher.find()) {
            start = forwardslashAbbreviationMatcher.group(1);
            nodeText = forwardslashAbbreviationMatcher.group(2);
        }
        SentenceState sentenceState = new SentenceState();
        nodeText = mapToken(TOKENISATION_PATTERN, nodeText,
                (token, matcher) ->
                        mapWord(token, sentenceState), true);
        if (!start.isEmpty()) {
            nodeText = start + nodeText;
        }
        nodeText = mapToken(NEWLINE, nodeText, (token, matcher) -> " ", true);
        nodeText = mapToken(ABBREVIATIONS, nodeText, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK) + ".", true);
        nodeText = mapToken(MULTIPLE_SPACES, nodeText, (token, matcher) -> " ", true);
        nodeText = mapToken(MIXED_ALPHANUMERIC, nodeText, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK), true);
        nodeText = mapToken(MATCHES_ENTITY, nodeText, (token, matcher) ->
                ENTITIES.contains(token.toUpperCase(Locale.UK))
                        ? token.toUpperCase(Locale.UK)
                        : token, true);
        return nodeText.trim();
    }

    private String mapWord(String token, SentenceState sentenceState) {
        Possessiveness possessive = isPossessive(token);
        if (possessive.possessive) {
            sentenceState.setEndOfSentence(possessive.endOfSentence);
            sentenceState.setMatchingBracket(possessive.openingBrackets);
            return token.toUpperCase(Locale.UK);
        }
        token = token.toLowerCase(Locale.UK);
        if (sentenceState.isEndOfSentence()) {
            token = mapToken(FIRST_LETTER,
                    token, (t, m) -> t.toUpperCase(Locale.UK), false);
            sentenceState.setMatchingBracket(token.matches("^[\\[(].*$"));
        }
        Matcher generalAbbreviationMatcher = GENERAL_ABBREVIATION.matcher(token);
        SentenceTerminationState terminationState = isEndOfSentence(token);
        sentenceState.setEndOfSentence(!generalAbbreviationMatcher.find()
                && (terminationState == SentenceTerminationState.TERMINATED
                || (terminationState == SentenceTerminationState.TERMINATED_WITH_BRACKET
                && sentenceState.isMatchingBracket())));
        return token;
    }

    private enum SentenceTerminationState {
        NOT_TERMINATED, TERMINATED, TERMINATED_WITH_BRACKET
    }

    private static SentenceTerminationState isEndOfSentence(String token) {
        if (StringUtils.isEmpty(token)) {
            return SentenceTerminationState.NOT_TERMINATED;
        }

        boolean singleLetter = false;
        boolean terminator = false;
        boolean endSpace = false;
        boolean closingBracket = false;

        String current;
        for (int i = 0; i < token.length(); i++) {
            current = Character.toString(token.charAt(i));
            if (FIRST_LETTER.matcher(current).matches()) {
                singleLetter = true;
                terminator = false;
                endSpace = false;
                closingBracket = false;
            } else if (SENTENCE_TERMINATOR.matcher(current).matches()) {
                terminator = true;
                closingBracket = false;
            } else if (CLOSING_BRACKET.matcher(current).matches()) {
                closingBracket = true;
            } else if (WHITESPACE.matcher(current).matches() && i == token.length() - 1) {
                endSpace = true;
            }
        }
        if (singleLetter && terminator && closingBracket && endSpace) {
            return SentenceTerminationState.TERMINATED_WITH_BRACKET;
        } else if (singleLetter && terminator && endSpace) {
            return SentenceTerminationState.TERMINATED;
        } else {
            return SentenceTerminationState.NOT_TERMINATED;
        }
    }

    private static class SentenceState {

        private boolean endOfSentence = true;
        private boolean matchingBracket = false;

        private boolean isEndOfSentence() {
            return endOfSentence;
        }

        private void setEndOfSentence(boolean endOfSentence) {
            this.endOfSentence = endOfSentence;
        }

        private boolean isMatchingBracket() {
            return matchingBracket;
        }

        private void setMatchingBracket(boolean matchingBracket) {
            this.matchingBracket = matchingBracket;
        }
    }

    private static class Possessiveness {

        boolean possessive;
        boolean openingBrackets;
        boolean endOfSentence;

        Possessiveness(boolean possessive, boolean openingBrackets, boolean endOfSentence) {
            this.possessive = possessive;
            this.openingBrackets = openingBrackets;
            this.endOfSentence = endOfSentence;
        }

        Possessiveness() {
            this(false, false, false);
        }
    }

    private static Possessiveness isPossessive(String token) {
        Possessiveness result = new Possessiveness();
        if (StringUtils.isEmpty(token)) {
            return NON_POSSESSIVE;
        }
        token = token.toUpperCase(Locale.UK);
        for (int i = 0; i < token.length(); i++) {
            String letter = Character.toString(token.charAt(i));
            if (OPENING_BRACKET.matcher(letter).matches() && !result.possessive) {
                result.openingBrackets = true;
            } else if ("I".equals(letter.toUpperCase(Locale.UK)) && !result.possessive) {
                result.possessive = true;
                result.endOfSentence = false;
            } else if (SENTENCE_TERMINATOR.matcher(letter).matches()) {
                result.endOfSentence = true;
            } else if (FIRST_LETTER.matcher(letter).matches()) {
                return NON_POSSESSIVE;
            }
        }
        return result;
    }
}
