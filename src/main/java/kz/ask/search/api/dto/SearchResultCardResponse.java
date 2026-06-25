package kz.ask.search.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SearchResultCardResponse {

    private UUID id;
    private String type;
    private String name;
    private String supplierName;
    private String branchAddress;
    private String categoryName;
    private String priceText;
    private String source;
    private String publicNote;
    private List<String> contactActions;
}
