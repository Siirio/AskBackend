package kz.ask.business.category.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryAutocompleteResponse {
    private List<CategorySuggestionResponse> suggestions;
}
