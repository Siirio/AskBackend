package kz.ask.search.basic.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;

public interface SearchOutboxClaimService {

    List<SearchOutboxEventDto> claim(Integer batchSize, String workerId);

    void retry(SearchOutboxEventDto event, RuntimeException failure);

    void complete(UUID eventId, String workerId, String outcome);
}
