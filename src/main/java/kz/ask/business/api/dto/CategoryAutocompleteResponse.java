package kz.ask.business.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryAutocompleteResponse {
    private List<CategorySuggestion> standard;
    private List<CategorySuggestion> custom;

    @Getter
    @Builder
    public static class CategorySuggestion {
        private String label;
        private UUID categoryId;
        private UUID parentId;
        private String parentLabel;
        private String path;
        private Boolean custom;
    }
}
