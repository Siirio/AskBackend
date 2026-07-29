package kz.ask.search.search_query_enrichment.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kz.ask.search.basic.application.processor.SearchInterpretation;
import kz.ask.search.search_query_enrichment.api.dto.SearchIntentStructureRequest;
import kz.ask.search.search_query_enrichment.infrastructure.client.DeepSeekSearchIntentStructurer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LayeredSearchIntentStructurer implements SearchIntentStructurer {

    private static final Pattern MAX_PRICE_PATTERN = Pattern.compile(
            "(?:до|не\\s+дороже|максимум|under|below|up\\s+to|max(?:imum)?|no\\s+more\\s+than)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");
    private static final Pattern MIN_PRICE_PATTERN = Pattern.compile(
            "(?:от|минимум|over|above|from|min(?:imum)?|at\\s+least)\\s*(\\d+[\\d\\s]*)(к|k|тыс|тысяч|тг)?");

    private final DeepSeekSearchIntentStructurer aiStructurer;

    @Override
    public SearchInterpretation interpret(SearchIntentStructureRequest request) {
        if (!aiStructurer.isAvailable()) {
            return rawInterpretation(request);
        }
        try {
            return aiStructurer.interpret(request);
        } catch (RuntimeException failure) {
            log.warn("AI query enhancement is unavailable: {}. Using raw interpretation.",
                    rootCauseMessage(failure));
            return rawInterpretation(request);
        }
    }

    private SearchInterpretation rawInterpretation(SearchIntentStructureRequest request) {
        String query = normalize(request.getRawQuery());
        return SearchInterpretation.builder()
                .normalizedQuery(query)
                .inferredMinPrice(parsePrice(query, MIN_PRICE_PATTERN))
                .inferredMaxPrice(parsePrice(query, MAX_PRICE_PATTERN))
                .inferredCity(null)
                .ambiguity("LOW")
                .suggestions(List.of())
                .build();
    }

    private BigDecimal parsePrice(String query, Pattern pattern) {
        Matcher matcher = pattern.matcher(query);
        if (!matcher.find()) {
            return null;
        }
        BigDecimal value = new BigDecimal(matcher.group(1).replace(" ", ""));
        String suffix = normalize(matcher.group(2));
        if (List.of("к", "k", "тыс", "тысяч").contains(suffix)) {
            return value.multiply(BigDecimal.valueOf(1000L));
        }
        return value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String rootCauseMessage(Throwable failure) {
        Throwable rootCause = failure;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() == null
                ? rootCause.getClass().getSimpleName()
                : rootCause.getMessage();
    }
}
