package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessContactDto;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessContactRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessContactServiceImpl implements BusinessContactService {

    private final BusinessContactRepository businessContactRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public BusinessContactDto create(UUID businessId, UUID branchId, ContactType type, String value) {
        Business business = businessRepository.getReferenceById(businessId);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);
        BusinessContact entity = businessContactRepository.save(
                businessMapper.toContactEntity(business, branch, type, value));
        return businessMapper.toBusinessContactDto(entity);
    }
}
