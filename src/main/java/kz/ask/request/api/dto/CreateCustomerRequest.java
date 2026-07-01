package kz.ask.request.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
    @NotBlank String query_text,
    @NotBlank String scope,
    @NotBlank String city_name
) {}
