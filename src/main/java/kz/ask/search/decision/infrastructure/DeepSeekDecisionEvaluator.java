package kz.ask.search.decision.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.ai.infrastructure.client.BaseDeepSeekClient;
import kz.ask.search.basic.application.decision.CriterionAssessment;
import kz.ask.search.basic.application.decision.CriterionEvidence;
import kz.ask.search.basic.application.decision.DecisionContext;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.decision.application.CandidateEvaluation;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekDecisionEvaluator extends BaseDeepSeekClient {

    private static final int MAX_CANDIDATES = 12;

    private final RestClient deepSeekRestClient;
    private final ObjectMapper objectMapper;

    @Value("${ask.ai.decision.max-tokens:1200}")
    private Integer maxTokens;

    @Value("classpath:prompts/decision-evaluator.md")
    private Resource promptResource;

    public List<CandidateEvaluation> evaluate(DecisionContext context, List<SearchDocumentDto> candidates) {
        if (!StringUtils.hasText(apiKey)) {
            return List.of();
        }
        if (context == null || candidates.isEmpty()) {
            return List.of();
        }
        List<SearchDocumentDto> bounded = candidates.size() > MAX_CANDIDATES
                ? candidates.subList(0, MAX_CANDIDATES)
                : candidates;
        try {
            JsonNode result = callDeepSeek(context, bounded);
            return parseEvaluations(result, bounded);
        } catch (RestClientException | JsonProcessingException ex) {
            log.warn("Decision evaluator failed, returning empty evaluations", ex);
            return List.of();
        }
    }

    private List<CandidateEvaluation> parseEvaluations(JsonNode result, List<SearchDocumentDto> candidates) {
        JsonNode evaluations = result.path("evaluations");
        if (!evaluations.isArray()) {
            return List.of();
        }
        Map<UUID, SearchDocumentDto> docsById = candidates.stream()
                .collect(java.util.stream.Collectors.toMap(
                        SearchDocumentDto::getAggregateId, d -> d, (a, b) -> a));
        List<CandidateEvaluation> list = new ArrayList<>();
        for (JsonNode node : evaluations) {
            UUID resultId;
            try {
                resultId = UUID.fromString(node.path("result_id").asText(""));
            } catch (IllegalArgumentException e) {
                continue;
            }
            SearchDocumentDto doc = docsById.get(resultId);
            List<CriterionAssessment> assessments = parseAssessments(
                    node.path("criterion_assessments"), doc);
            String decisionLabel = parseNullableText(node.path("decision_label"));
            if (decisionLabel != null && assessments.stream()
                    .anyMatch(a -> "FAIL".equals(a.getStatus()))) {
                decisionLabel = null;
            }
            list.add(CandidateEvaluation.builder()
                    .resultId(resultId)
                    .decisionLabel(decisionLabel)
                    .criterionAssessments(assessments)
                    .advantages(parseStringArray(node.path("advantages")))
                    .tradeoffs(parseStringArray(node.path("tradeoffs")))
                    .unknowns(parseStringArray(node.path("unknowns")))
                    .comparisonFacts(parseEvidence(node.path("comparison_facts")))
                    .build());
        }
        return list;
    }

    private List<CriterionAssessment> parseAssessments(JsonNode assessmentsNode, SearchDocumentDto doc) {
        List<CriterionAssessment> assessments = new ArrayList<>();
        if (!assessmentsNode.isArray()) {
            return assessments;
        }
        for (JsonNode node : assessmentsNode) {
            List<CriterionEvidence> evidence = parseEvidence(node.path("evidence"));
            String status = node.path("status").asText("UNKNOWN");
            if (hasInvalidEvidence(evidence, doc)) {
                status = "UNKNOWN";
            }
            assessments.add(CriterionAssessment.builder()
                    .criterionKey(node.path("criterion_key").asText(""))
                    .label(node.path("label").asText(""))
                    .status(status)
                    .displayValue(parseNullableText(node.path("display_value")))
                    .consequence(parseNullableText(node.path("consequence")))
                    .evidence(evidence)
                    .build());
        }
        return assessments;
    }

    private boolean hasInvalidEvidence(List<CriterionEvidence> evidence, SearchDocumentDto doc) {
        if (doc == null || evidence.isEmpty()) {
            return false;
        }
        Map<String, Object> attrs = doc.getVerifiedAttributes() != null
                ? doc.getVerifiedAttributes() : Collections.emptyMap();
        for (CriterionEvidence e : evidence) {
            String source = e.getSource();
            String key = e.getKey();
            if ("VERIFIED_ATTRIBUTE".equals(source)) {
                if (!attrs.containsKey(key)) {
                    return true;
                }
            } else if ("CANONICAL_FIELD".equals(source)) {
                if (!isValidCanonicalField(key)) {
                    return true;
                }
            } else if ("BUSINESS_PROFILE".equals(source)) {
                if (!"business_name".equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidCanonicalField(String key) {
        return java.util.Set.of("price", "currency", "category_label", "title", "city")
                .contains(key);
    }

    private List<CriterionEvidence> parseEvidence(JsonNode evidenceNode) {
        List<CriterionEvidence> evidence = new ArrayList<>();
        if (!evidenceNode.isArray()) {
            return evidence;
        }
        for (JsonNode node : evidenceNode) {
            evidence.add(CriterionEvidence.builder()
                    .source(node.path("source").asText(""))
                    .key(node.path("key").asText(""))
                    .value(node.path("value").asText(""))
                    .build());
        }
        return evidence;
    }

    private List<String> parseStringArray(JsonNode arrayNode) {
        List<String> items = new ArrayList<>();
        if (!arrayNode.isArray()) {
            return items;
        }
        for (JsonNode node : arrayNode) {
            String text = node.asText("");
            if (!text.isBlank()) {
                items.add(text);
            }
        }
        return items;
    }

    private String parseNullableText(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText("");
        return text.isBlank() ? null : text;
    }

    private JsonNode callDeepSeek(DecisionContext context, List<SearchDocumentDto> candidates)
            throws JsonProcessingException {
        JsonNode response = deepSeekRestClient.post()
                .uri(CHAT_COMPLETIONS_PATH)
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", readPrompt(promptResource)),
                                Map.of("role", "user", "content", objectMapper.writeValueAsString(Map.of(
                                        "decision_context", toContextJson(context),
                                        "candidates", toCandidatesJson(candidates)
                                )))
                        ),
                        "response_format", Map.of("type", "json_object"),
                        "stream", false,
                        "max_tokens", maxTokens
                ))
                .retrieve()
                .body(JsonNode.class);
        String outputText = response.path("choices").path(0).path("message").path("content").asText("");
        if (!StringUtils.hasText(outputText)) {
            throw new ExternalServiceException(ErrorCode.AI_INTENT_STRUCTURE_FAILED);
        }
        return objectMapper.readTree(outputText);
    }

    private Map<String, Object> toContextJson(DecisionContext context) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("hard_constraints", context.getHardConstraints().stream()
                .map(this::toCriterionJson).toList());
        json.put("preferences", context.getPreferences().stream()
                .map(this::toCriterionJson).toList());
        json.put("use_cases", context.getUseCases().stream()
                .map(uc -> Map.of("key", uc.getKey(), "label", uc.getLabel())).toList());
        json.put("exclusions", context.getExclusions().stream()
                .map(this::toCriterionJson).toList());
        if (context.getCustomText() != null) {
            json.put("custom_text", context.getCustomText());
        }
        return json;
    }

    private Map<String, Object> toCriterionJson(kz.ask.search.basic.application.decision.DecisionCriterion c) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("key", c.getKey() != null ? c.getKey() : "");
        json.put("label", c.getLabel() != null ? c.getLabel() : "");
        json.put("operator", c.getOperator() != null ? c.getOperator() : "EQ");
        json.put("values", c.getValues() != null ? c.getValues() : List.of());
        if (c.getUnit() != null) {
            json.put("unit", c.getUnit());
        }
        return json;
    }

    private List<Map<String, Object>> toCandidatesJson(List<SearchDocumentDto> candidates) {
        return candidates.stream().map(doc -> {
            Map<String, Object> candidate = new LinkedHashMap<>();
            candidate.put("result_id", doc.getAggregateId().toString());
            candidate.put("title", doc.getTitle());
            Map<String, Object> canonical = new LinkedHashMap<>();
            canonical.put("price", doc.getPrice());
            canonical.put("currency", doc.getCurrency());
            canonical.put("category_label", doc.getCategoryLabel());
            candidate.put("canonical_fields", canonical);
            candidate.put("verified_attributes", doc.getVerifiedAttributes() != null
                    ? doc.getVerifiedAttributes() : Collections.emptyMap());
            Map<String, Object> bizProfile = new LinkedHashMap<>();
            bizProfile.put("business_name", doc.getBusinessName());
            candidate.put("business_profile", bizProfile);
            Map<String, Object> branch = new LinkedHashMap<>();
            branch.put("city", doc.getCity());
            candidate.put("branch", branch);
            return candidate;
        }).toList();
    }
}
