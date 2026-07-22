package kz.ask.business.category.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategorySuggestionResponse {
    private UUID categoryId;
    private String label;
    private String type;
    private String source;
}
