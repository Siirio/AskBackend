package kz.ask.request.api.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CreateCustomerRequestRequest {

    @NotBlank
    private String queryText;

    @NotBlank
    private String scope;

    @NotBlank
    private String cityName;
}
