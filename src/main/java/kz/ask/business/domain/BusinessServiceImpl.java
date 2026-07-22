package kz.ask.business.domain;

import java.util.UUID;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.dto.BusinessDto;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.dto.BusinessRegistrationResult;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BusinessRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessBranchService businessBranchService;
    private final BusinessMemberService businessMemberService;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional
    public BusinessRegistrationResult registerBusiness(UUID ownerId,
                                                        String businessName,
                                                        String branchName,
                                                        UUID branchCityId,
                                                        String branchAddress,
                                                        Boolean onlineOnly,
                                                        String contactEmail) {
        Business business = businessRepository.save(businessMapper.toBusinessEntity(businessName));
        UUID businessId = business.getId();
        BusinessDto businessDto = businessMapper.toBusinessDto(business);

        BusinessBranchDto branchDto = businessBranchService.create(
                businessId, branchCityId, branchName, branchAddress, null, onlineOnly, null, null);

        BusinessMemberDto memberDto = businessMemberService.createOwner(businessId, ownerId);

        return BusinessRegistrationResult.builder()
                .business(businessDto)
                .branch(branchDto)
                .member(memberDto)
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
                                .build();
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        return businessMemberService.isOwnerOfBusiness(businessId, userId);
    }

    @Override
    public Boolean isManagerOrAboveOfBusiness(UUID businessId, UUID userId) {
        return businessMemberService.isManagerOrAboveOfBusiness(businessId, userId);
    }

    @Override
    @Transactional
    public void updateBusiness(UUID businessId, String name) {
        Business business = businessRepository.getReferenceById(businessId);
        if (name != null) business.setName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessRegistrationResult findByMember(UUID userId) {
        BusinessMemberDto memberDto = businessMemberService.findByUser(userId);
        if (memberDto == null) return null;

        BusinessDto businessDto = businessRepository.findById(memberDto.getBusinessId())
                .map(businessMapper::toBusinessDto)
                .orElse(null);

        return BusinessRegistrationResult.builder()
                .business(businessDto)
                .member(memberDto)
                                .build();
    }
}
