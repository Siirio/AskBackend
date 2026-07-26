package kz.ask.shared.api.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorResponse {

    private Instant timestamp;
    private String errorCode;
    private String message;
    private List<ErrorDetail> errors;
}
