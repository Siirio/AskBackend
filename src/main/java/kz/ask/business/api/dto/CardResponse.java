package kz.ask.business.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private UUID businessId;
    private List<CardBlockDto> blocks;
    private Instant publishedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardBlockDto {
        private String localId;
        private String blockType;
        private Integer displayOrder;
        private Object config;
    }
}
