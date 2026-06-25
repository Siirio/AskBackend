package kz.ask.business.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private List<CategoryResponse> children;
}
