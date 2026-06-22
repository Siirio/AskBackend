package kz.ask.search.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.catalog.domain.entity.Product;
import kz.ask.catalog.domain.entity.ProductOffer;
import kz.ask.search.domain.entity.SearchDocument;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchMapper {

    private final ObjectMapper objectMapper;

    public SearchDocument toSearchDocument(ProductOffer offer, Product product, Business business,
                                            BusinessBranch branch) {
        SearchDocument doc = new SearchDocument();
        doc.setDocumentType(SearchDocumentType.PRODUCT);
        doc.setProductOffer(offer);
        doc.setServiceBranchOffer(null);
        doc.setTitle(product.getName());
        doc.setSummary(product.getDescription());
        doc.setCategoryLabel(product.getCategoryLabel());
        doc.setSku(product.getSku());
        doc.setCharacteristicsJson(product.getCharacteristicsJson());
        doc.setBusiness(business);
        doc.setBranch(branch);
        doc.setPrice(offer.getPrice());
        doc.setStatus(RecordStatus.ACTIVE);

        List<String> tokens = new ArrayList<>();
        addTokens(tokens, product.getName());
        addTokens(tokens, product.getCategoryLabel());
        addTokens(tokens, product.getDescription());
        addTokens(tokens, product.getSku());
        if (product.getTags() != null) {
            for (String tag : product.getTags()) {
                addTokens(tokens, tag);
            }
        }
        Map<String, String> chars = parseNormalized(product.getCharacteristicsJson());
        tokens.addAll(chars.keySet());
        tokens.addAll(chars.values());
        addTokens(tokens, business.getName());
        addTokens(tokens, branch.getName());
        doc.setTokens(tokens.stream().distinct().collect(Collectors.toList()));

        return doc;
    }

    private void addTokens(List<String> tokens, String text) {
        if (text == null || text.isEmpty()) return;
        String lower = text.toLowerCase();
        tokens.add(lower);
        Arrays.stream(lower.split("[\\s,;]+"))
            .filter(t -> !t.isEmpty())
            .forEach(tokens::add);
    }

    private Map<String, String> parseNormalized(String json) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        } catch (Exception e) {
            return Map.of();
        }
    }
}
