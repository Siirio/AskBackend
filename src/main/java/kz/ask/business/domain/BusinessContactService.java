package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessContactDto;
import kz.ask.business.domain.enums.ContactType;

public interface BusinessContactService {

    BusinessContactDto create(UUID businessId, UUID branchId, ContactType type, String value);
}
