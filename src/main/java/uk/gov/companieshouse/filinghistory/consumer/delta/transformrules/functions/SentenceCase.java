package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SentenceCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
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

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);
        String nodeText = outputNode.at("/" + arguments.getFirst().replace(".", "/"))
                .textValue();

        // TODO Apply Perl sentence_case transformation to node text
        String transformedText = "TODO: Sentence case: " + nodeText;

        outputNode.put(finalField, transformedText);
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
        nodeText = Transformer.mapToken(TOKENISATION_PATTERN, nodeText,
                (token, matcher) ->
                        mapWord(token, sentenceState), true);
        if (!start.isEmpty()) {
            nodeText = start + nodeText;
        }
        nodeText = Transformer.mapToken(NEWLINE, nodeText, (token, matcher) -> " ", true);
        nodeText = Transformer.mapToken(ABBREVIATIONS, nodeText, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK) + ".", true);
        nodeText = Transformer.mapToken(MULTIPLE_SPACES, nodeText, (token, matcher) -> " ", true);
        nodeText = Transformer.mapToken(MIXED_ALPHANUMERIC, nodeText, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK), true);
        nodeText = Transformer.mapToken(MATCHES_ENTITY, nodeText, (token, matcher) ->
                ENTITIES.contains(token.toUpperCase(Locale.UK))
                        ? token.toUpperCase(Locale.UK)
                        : token, true);
        return nodeText.trim();
    }

    private static String mapWord(String token, SentenceState sentenceState) {
        Possessiveness possessive = isPossessive(token);
        if (possessive.possessive) {
            sentenceState.setEndOfSentence(possessive.endOfSentence);
            sentenceState.setMatchingBracket(possessive.openingBrackets);
            return token.toUpperCase(Locale.UK);
        }
        token = token.toLowerCase(Locale.UK);
        if (sentenceState.isEndOfSentence()) {
            token = Transformer.mapToken(FIRST_LETTER,
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

    enum SentenceTerminationState {
        NOT_TERMINATED, TERMINATED, TERMINATED_WITH_BRACKET
    }

    static SentenceTerminationState isEndOfSentence(String token) {
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

    static class Possessiveness {

        boolean possessive;
        boolean openingBrackets;
        boolean endOfSentence;

        Possessiveness() {
        }

        Possessiveness(boolean possessive, boolean openingBrackets, boolean endOfSentence) {
            this.possessive = possessive;
            this.openingBrackets = openingBrackets;
            this.endOfSentence = endOfSentence;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Possessiveness that = (Possessiveness) other;
            return possessive == that.possessive
                    && openingBrackets == that.openingBrackets
                    && endOfSentence == that.endOfSentence;
        }

        @Override
        public int hashCode() {
            return Objects.hash(possessive, openingBrackets, endOfSentence);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (possessive) {
                result.append("possessive");
            } else {
                result.append("not possessive");
                return result.toString();
            }
            if (openingBrackets) {
                result.append(", opening brackets");
            } else {
                result.append(", no opening brackets");
            }
            if (endOfSentence) {
                result.append(", end of sentence");
            } else {
                result.append(", not end of sentence");
            }
            return result.toString();
        }
    }

    static Possessiveness isPossessive(String token) {
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
