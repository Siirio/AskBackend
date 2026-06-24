package kz.ask.service.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import kz.ask.request.domain.enums.SupplierResponseStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixServiceBookingRequest {

    @NotNull
    private SupplierResponseStatus status;

    private Instant proposedStartAt;

    private Instant confirmedStartAt;

    private Instant confirmedEndAt;

    private String providerNote;
}
