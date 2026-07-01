package kz.ask.request.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SupplierTaskDetailResponse {
    private UUID id;
    private String query;
    private String scope;
    private String customerName;
    private String customerContact;
    private String city;
    private String categoryName;
    private long ageMinutes;
    private String status;
    private Instant createdAt;
    private List<ResponseMessage> messages;

    @Getter
    @Setter
    @Builder
    public static class ResponseMessage {
        private UUID id;
        private String role;
        private String text;
        private String status;
        private String price;
        private Instant createdAt;
    }
}
