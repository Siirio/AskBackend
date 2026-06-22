package kz.ask.catalog.api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveResponse {
    private UUID importId;
    private String status;
    private Integer productsCreated;
    private Integer offersCreated;
    private Integer rowsSkipped;
}
