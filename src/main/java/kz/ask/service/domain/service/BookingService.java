package kz.ask.service.domain.service;

import java.time.Instant;
import java.util.UUID;

public interface BookingService {

    void createBookingFromRequest(UUID customerId, UUID serviceBranchOfferId,
                                  UUID branchId, Instant requestedStartAt,
                                  Instant confirmedStartAt, Instant confirmedEndAt);
}
