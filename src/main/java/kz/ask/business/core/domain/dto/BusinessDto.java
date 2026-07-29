package kz.ask.business.core.domain.dto;

import java.util.UUID;
import kz.ask.business.core.domain.enums.BusinessScope;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BusinessDto {

    private UUID id;
    private String name;
    private UUID categoryId;
    private String categoryName;
    private String currency;
    private BusinessScope scope;
    private Boolean onlineOnly;
}
