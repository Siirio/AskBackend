package kz.ask.search.decision.api.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompareValueResponse {

    private UUID resultId;
    private String value;
    private String status;
    private String highlight;
}
