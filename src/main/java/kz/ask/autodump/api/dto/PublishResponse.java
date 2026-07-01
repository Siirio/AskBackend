package kz.ask.autodump.api.dto;

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
public class PublishResponse {

    private UUID sessionId;
    private String sessionStatus;
    private Integer published;
    private Integer skipped;
}
