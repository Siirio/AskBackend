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
public class CreateAutodumpSessionResponse {

    private UUID sessionId;
    private String status;
    private UUID rawInputId;
    private UUID aiJobId;
    private Integer draftsCreated;
}
