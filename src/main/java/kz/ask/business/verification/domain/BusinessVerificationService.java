package kz.ask.business.verification.domain;

import java.util.UUID;
import kz.ask.business.verification.domain.dto.BusinessVerificationDto;

public interface BusinessVerificationService {

    BusinessVerificationDto create(UUID businessId, BusinessVerificationDto verification);
}
