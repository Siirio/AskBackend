package kz.ask.business.profile.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;

public interface BusinessProfileService {
    BusinessProfileDto findByBusinessId(UUID businessId);

    Map<UUID, BusinessProfileDto> findByBusinessIds(Collection<UUID> businessIds);

    BusinessProfileDto save(UUID businessId, String brandColor, String logoUrl, String coverUrl,
                            String description, String number, String email,
                            String instagramUrl, String telegramUrl, String websiteUrl,
                            DeliveryCoverage deliveryCoverage, List<String> deliveryCities,
                            Boolean pickupAvailable);
}
