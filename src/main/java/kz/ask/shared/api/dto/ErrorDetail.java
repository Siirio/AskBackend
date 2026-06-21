package kz.ask.shared.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErrorDetail {

    private String field;
    private String message;
}
