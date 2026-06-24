package kz.ask.request.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.request.domain.dto.ActivityDto;
import kz.ask.request.domain.dto.ServiceRequestResult;
import kz.ask.request.domain.enums.SupplierResponseStatus;

public interface ServiceRequestService {

    List<ActivityDto> listActivity(UUID branchId);

    ServiceRequestResult handleRequest(UUID branchId, UUID customerRequestId,
                                       SupplierResponseStatus status,
                                       Instant proposedStartAt,
                                       Instant confirmedStartAt,
                                       Instant confirmedEndAt,
                                       String providerNote);
}
