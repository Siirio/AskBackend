package kz.ask.search.basic.domain;

import kz.ask.search.basic.domain.dto.SearchOutboxEventDto;

public interface SearchIndexDeliveryService {

    String deliver(SearchOutboxEventDto event);
}
