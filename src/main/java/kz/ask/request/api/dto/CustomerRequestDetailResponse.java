package kz.ask.request.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomerRequestDetailResponse {
    private UUID id;
    private String query;
    private String scope;
    private String city;
    private String status;
    private int matchedSuppliers;
    private Instant createdAt;
    private List<SupplierReplyItem> replies;

    @Getter
    @Setter
    @Builder
    public static class SupplierReplyItem {
        private UUID id;
        private String supplierName;
        private String branchName;
        private String status;
        private String statusLabel;
        private BigDecimal price;
        private String productHint;
        private String comment;
        private Instant createdAt;
    }
}
