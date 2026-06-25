package kz.ask.request.api.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CustomerRequestHistoryItem {
    private UUID id;
    private String query;
    private String scope;
    private String city;
    private String status;
    private int matchedSuppliers;
    private int replyCount;
    private Instant createdAt;
}
