package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.dto.BusinessContactDto;
import kz.ask.business.domain.dto.BusinessDto;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.dto.BusinessRegistrationResult;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.shared.domain.enums.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessBranchService businessBranchService;
    private final BusinessMemberService businessMemberService;
    private final BusinessContactService businessContactService;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public BusinessRegistrationResult registerBusiness(UUID ownerId,
                                                        String businessName,
                                                        String branchName,
                                                        UUID branchCityId,
                                                        String branchAddress,
                                                        Boolean onlineOnly,
                                                        String contactEmail,
                                                        String contactPhone) {
        Business business = businessRepository.save(businessMapper.toBusinessEntity(businessName));
        UUID businessId = business.getId();
        BusinessDto businessDto = businessMapper.toBusinessDto(business);

        BusinessBranchDto branchDto = businessBranchService.create(
                businessId, branchCityId, branchName, branchAddress, onlineOnly, null, null);

        BusinessMemberDto memberDto = businessMemberService.createOwner(businessId, ownerId);

        BusinessContactDto contactDto = null;
        if (contactEmail != null) {
            contactDto = businessContactService.create(businessId, branchDto.getId(), ContactType.EMAIL, contactEmail);
        } else if (contactPhone != null) {
            contactDto = businessContactService.create(businessId, branchDto.getId(), ContactType.PHONE, contactPhone);
        }

        return BusinessRegistrationResult.builder()
                .business(businessDto)
                .branch(branchDto)
                .member(memberDto)
                .contact(contactDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessRegistrationResult findByOwner(UUID userId) {
        BusinessMemberDto memberDto = businessMemberService.findByOwner(userId);
        if (memberDto == null) return null;

        BusinessDto businessDto = businessRepository.findById(memberDto.getBusinessId())
                .map(businessMapper::toBusinessDto)
                .orElse(null);

        BusinessBranchDto branchDto = businessBranchService.findFirstByBusinessId(memberDto.getBusinessId());

        return BusinessRegistrationResult.builder()
                .business(businessDto)
                .branch(branchDto)
                .member(memberDto)
                .contact(null)
                .build();
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        return businessMemberService.isOwnerOfBusiness(businessId, userId);
    }

    @Override
    @Transactional
    public void updateBusiness(UUID businessId, String name) {
        Business business = businessRepository.getReferenceById(businessId);
        if (name != null) business.setName(name);
    }
}
