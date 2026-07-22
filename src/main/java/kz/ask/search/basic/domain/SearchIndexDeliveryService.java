package kz.ask.search.basic.domain;

import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;
import kz.ask.search.basic.domain.dto.SearchProjectionResult;

public interface SearchIndexDeliveryService {

    String deliver(SearchOutboxEventDto event, SearchProjectionResult projection);
}
