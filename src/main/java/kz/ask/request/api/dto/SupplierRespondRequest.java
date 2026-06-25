package kz.ask.request.api.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRespondRequest {
    private String status;
    private BigDecimal price;
    private String productHint;
    private String comment;
}
