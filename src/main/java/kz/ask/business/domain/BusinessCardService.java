package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessCardDto;

public interface BusinessCardService {
    BusinessCardDto findByBusiness(UUID businessId);
    BusinessCardDto saveDraft(UUID businessId, String blocks);
    BusinessCardDto publish(UUID businessId);
}
