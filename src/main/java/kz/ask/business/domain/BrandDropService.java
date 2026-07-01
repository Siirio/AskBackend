package kz.ask.business.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.dto.BrandDropDto;

public interface BrandDropService {
    List<BrandDropDto> listPublic(UUID businessId);

    List<BrandDropDto> listOwner(UUID businessId);

    BrandDropDto create(UUID businessId, String name, String description, Instant startDate,
                        Instant endDate, String type, String status, String coverUrl);
}
