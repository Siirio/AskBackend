package kz.ask.business.verification.domain;

import java.util.UUID;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.verification.domain.dto.BusinessVerificationDto;
import kz.ask.business.verification.domain.entity.BusinessVerification;
import kz.ask.business.verification.infrastructure.mapper.BusinessVerificationMapper;
import kz.ask.business.verification.infrastructure.repository.BusinessVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessVerificationServiceImpl implements BusinessVerificationService {

    private final BusinessVerificationRepository businessVerificationRepository;
    private final BusinessRepository businessRepository;
    private final BusinessVerificationMapper businessVerificationMapper;

    @Override
    @Transactional
    public BusinessVerificationDto create(UUID businessId, BusinessVerificationDto verification) {
        BusinessVerification entity = businessVerificationMapper.toEntity(
                businessRepository.getReferenceById(businessId), verification);
        return businessVerificationMapper.toDto(businessVerificationRepository.save(entity));
    }
}
