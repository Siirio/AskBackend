package kz.ask.business.domain;

import java.util.UUID;
import java.util.Collection;
import java.util.Map;
import kz.ask.business.domain.dto.BrandProfileDto;

public interface BrandProfileService {
    BrandProfileDto findByBusinessId(UUID businessId);

    Map<UUID, BrandProfileDto> findByBusinessIds(Collection<UUID> businessIds);

    BrandProfileDto save(UUID businessId, String brandColor, String logoUrl, String coverUrl,
                         String toneOfVoice, String description, String instagramUrl,
                         String telegramUrl, String websiteUrl);
}
