package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SentenceCase extends AbstractTransformer {

    private static final Possessiveness NON_POSSESSIVE = new Possessiveness();
    private static final Set<String> ENDINGS = new HashSet<>(
            List.of("LIMITED", "LTD", "L.T.D", "PUBLIC LIMITED COMPANY", "PCC", "P.C.C.", "PLC",
                    "P.L.C", "UNLIMITED", "UNLTD", "COMMUNITY INTEREST COMPANY", "CIC",
                    "C.I.C", "COMMUNITY INTEREST PUBLIC LIMITED COMPANY", "COMMUNITY INTEREST P.L.C",
                    "COMMUNITY INTEREST PLC", "LIMITED LIABILITY PARTNERSHIP", "LLP",
                    "LIMITED PARTNERSHIP", "L.P", "LP", "EUROPEAN ECONOMIC INTEREST GROUPING",
                    "EEIG", "OPEN-ENDED INVESTMENT COMPANY", "OEIC", "INVESTMENT COMPANY WITH VARIABLE CAPITAL",
                    "ICVC", "AEIE", "ANGHYFYNGEDIG", "C.B.C", "C.C.C",
                    "CBC", "CBCN", "CBP", "CCC", "CWMNI BUDDIANT CYMUNEDOL", "CWMNI BUDDIANT CYMUNEDOL C.C.C",
                    "CWMNI BUDDIANT CYMUNEDOL CCC", "CWMNI BUDDIANT CYMUNEDOL CYHOEDDUS CYFYNGEDIG",
                    "CWMNI BUDDSODDIA CHYFALAF NEWIDIOL", "CWMNI BUDDSODDIANT PENAGORED",
                    "CWMNI CYFYNGEDIG CYHOEDDUS", "CYF", "CYFYNGEDIG",
                    "EESV", "EOFG", "EOOS", "GEIE", "GELE", "PAC", "PARTNERIAETH ATEBOLRWYDD CYFYNGEDIG",
                    "PARTNERIAETH CYFYNGEDIG",

                    "PCC LIMITED",                // protected cell company
                    "PCC LTD",                    // protected cell company
                    "PROTECTED CELL COMPANY",     // protected cell company
                    "CWMNI CELL GWARCHODEDIG",    // protected cell company
                    "CCG CYFYNGEDIG",             // protected cell company
                    "CCG CYF"                     // protected cell company
            ));

    private static final Set<String> OTHERS = new HashSet<>(List.of("ARD", "NI", "SE", "GB", "SC"));
    private static final Pattern FORWARD_SLASH_ABBREVIATION = Pattern.compile("^(.?/)(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOKENISATION_PATTERN = Pattern.compile("(\\S+(\\s+|$))");
    private static final Pattern NEWLINE = Pattern.compile("\\n");
    private static final Pattern ABBREVIATIONS = Pattern.compile("\\b([A-Z])[.]", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern MIXED_ALPHANUMERIC = Pattern.compile("(\\w+\\d+\\w*|\\d+\\w+)");
    private static final Pattern MATCHES_ENDING = Pattern.compile(
            "(\\b(?i:" + String.join("|", ENDINGS) + ")\\b)");
    private static final Pattern MATCHES_OTHER_CASE = Pattern.compile(
            "(\\b(?i:" + String.join("|", OTHERS) + ")\\b)");
    private static final Pattern MIXED_CASE = Pattern.compile("[a-z].*[A-Z]\\s?|[A-Z].*[a-z]\\s?");
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

    public SentenceCase(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        target.objectNode().put(target.fieldKey(), transformSentenceCase(target.fieldValue()));
    }

    String transformSentenceCase(String nodeText) {
        if (StringUtils.isEmpty(nodeText)) {
            return nodeText;
        }
        Matcher forwardslashAbbreviationMatcher = FORWARD_SLASH_ABBREVIATION.matcher(nodeText);
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
        nodeText = mapToken(MATCHES_ENDING, nodeText, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK), true);
        nodeText = mapToken(MATCHES_OTHER_CASE, nodeText, (token, matcher) ->
                matcher.group(1).toUpperCase(Locale.UK), true);

        return nodeText.trim();
    }

    private String mapWord(String token, SentenceState sentenceState) {
        Possessiveness possessive = isPossessive(token);
        if (possessive.possessive) {
            sentenceState.setEndOfSentence(possessive.endOfSentence);
            sentenceState.setMatchingBracket(possessive.openingBrackets);
            return token.toUpperCase(Locale.UK);
        }
        if (!MIXED_CASE.matcher(token).find()) {
            token = token.toLowerCase(Locale.UK);
        }
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

    private enum SentenceTerminationState {
        NOT_TERMINATED, TERMINATED, TERMINATED_WITH_BRACKET
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
}
