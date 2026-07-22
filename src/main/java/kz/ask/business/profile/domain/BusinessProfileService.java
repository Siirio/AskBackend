package kz.ask.business.profile.domain;

import java.util.UUID;
import java.util.Collection;
import java.util.Map;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;

public interface BusinessProfileService {
    BusinessProfileDto findByBusinessId(UUID businessId);

    Map<UUID, BusinessProfileDto> findByBusinessIds(Collection<UUID> businessIds);

    BusinessProfileDto save(UUID businessId, String brandColor, String logoUrl, String coverUrl,
                            String description, String number, String email,
                            String instagramUrl, String telegramUrl, String websiteUrl);
}
