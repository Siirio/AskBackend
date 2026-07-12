package kz.ask.search.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SearchDocumentService {

    void syncProductDocument(UUID productOfferId, UUID businessId, UUID branchId,
                             String title, String summary, String categoryLabel,
                             String sku, List<String> tags, BigDecimal price, Boolean live);

    void syncServiceDocument(UUID serviceBranchOfferId, UUID businessId, UUID branchId,
                             String title, String summary, String categoryLabel,
                             BigDecimal price, Boolean live);
}
