package kz.ask.search.domain;

import java.util.List;
import java.util.UUID;

public interface SearchDocumentService {

    void syncProductDocument(UUID productOfferId, String title, String summary, List<String> tags, Boolean live);
}
