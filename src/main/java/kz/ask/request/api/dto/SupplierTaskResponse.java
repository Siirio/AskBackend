package kz.ask.request.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SupplierTaskResponse {

    private UUID id;
    private String query;
    private String customerArea;
    private String categoryName;
    private long ageMinutes;
    private String confidenceCode;
    private String status;
}
