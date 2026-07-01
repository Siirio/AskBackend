package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BrandProfileDto;

public interface BrandProfileService {
    BrandProfileDto findByBusinessId(UUID businessId);

    BrandProfileDto save(UUID businessId, String brandColor, String logoUrl, String coverUrl,
                         String toneOfVoice, String description, String instagramUrl,
                         String telegramUrl, String websiteUrl);
}
