package kz.ask.business.api.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveCardRequest {
    private List<CardBlockDto> blocks;

    @Getter
    @Setter
    public static class CardBlockDto {
        private String localId;
        private String blockType;
        private Integer displayOrder;
        private Object config;
    }
}
