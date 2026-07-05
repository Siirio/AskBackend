package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BrandPageBlockDto;

public interface BrandPageBlockService {
    List<BrandPageBlockDto> listPublic(UUID businessId);

    List<BrandPageBlockDto> listDraft(UUID businessId);

    List<BrandPageBlockDto> replace(UUID businessId, List<BrandPageBlockDto> blocks);

    List<BrandPageBlockDto> replaceDraft(UUID businessId, List<BrandPageBlockDto> blocks);

    List<BrandPageBlockDto> publishDraft(UUID businessId);
}
