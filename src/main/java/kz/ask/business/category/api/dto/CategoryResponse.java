package kz.ask.business.category.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String type;
    private String source;
}
