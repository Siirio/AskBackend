package kz.ask.search.domain;

import kz.ask.search.domain.dto.SearchOutboxEventDto;
import kz.ask.search.domain.dto.SearchProjectionResult;

public interface SearchIndexDeliveryService {

    String deliver(SearchOutboxEventDto event, SearchProjectionResult projection);
}
