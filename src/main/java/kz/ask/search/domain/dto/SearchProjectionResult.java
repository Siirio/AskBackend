package kz.ask.search.domain.dto;

import java.util.UUID;
import kz.ask.search.domain.enums.SearchDocumentType;
import kz.ask.search.domain.enums.SearchProjectionAction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchProjectionResult {

    private SearchProjectionAction action;
    private SearchDocumentType documentType;
    private UUID aggregateId;
    private UUID documentId;
}
