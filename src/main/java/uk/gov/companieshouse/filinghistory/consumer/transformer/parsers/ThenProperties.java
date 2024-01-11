package uk.gov.companieshouse.filinghistory.consumer.transformer.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import uk.gov.companieshouse.filinghistory.consumer.transformer.Then;

public record ThenProperties(@JsonProperty("define") Map<String, String> define,
                             @JsonProperty("set") Map<String, ?> set,
                             @JsonProperty("exec") Map<String, List<String>> exec) {

    public Then compile() {
        // Parse the entries in the set, define and exec to:
        //  - set: build a map of field name -> a transformer function
//        Map<String, List<String>> setElements = set.entrySet().stream()
//                .map(entry -> )
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("Missing data.type or type data"));



        //  - exec: ?? field name -> Call a custom method to build the value. Check Perl ??
        //  - define: ?? Apply regex to some field. Check Perl ??
        Map<String, Pattern> defineElements = define != null ? define.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> Pattern.compile(e.getValue()))) : Map.of();

        return new Then(defineElements, Map.of(), Map.of());
    }


}
