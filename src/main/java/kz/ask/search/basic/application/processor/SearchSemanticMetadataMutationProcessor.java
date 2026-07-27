package kz.ask.search.basic.application.processor;

import java.util.Optional;
import kz.ask.search.basic.domain.SearchDocumentService;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.dto.SearchDocumentDto;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SearchSemanticMetadataMutationProcessor {

    private final SearchDocumentService searchDocumentService;
    private final SearchOutboxService searchOutboxService;

    @Transactional
    public Optional<Long> replace(SearchDocumentDto projection, Long expectedVersion) {
        Optional<Long> version = searchDocumentService.upsertSemanticMetadataIfVersion(
                projection, expectedVersion);
        version.ifPresent(value -> searchOutboxService.publish(
                projection.getDocumentType() == kz.ask.search.basic.domain.enums.SearchDocumentType.ITEM
                        ? SearchAggregateType.ITEM : SearchAggregateType.SERVICE,
                projection.getAggregateId(),
                SearchEventType.UPSERT,
                value));
        return version;
    }
}
