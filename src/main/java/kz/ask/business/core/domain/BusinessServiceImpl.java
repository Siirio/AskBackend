package kz.ask.business.core.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.core.domain.dto.BusinessDto;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.category.domain.CategoryService;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.business.member.domain.BusinessMemberService;
import kz.ask.business.core.domain.dto.BusinessRegistrationResult;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.profile.domain.BusinessProfileService;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;
import kz.ask.business.core.infrastructure.mapper.BusinessMapper;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ValidationException;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessBranchService businessBranchService;
    private final BusinessMemberService businessMemberService;
    private final BusinessProfileService businessProfileService;
    private final CategoryService categoryService;
    private final BusinessMapper businessMapper;

    @Override
    @Transactional(readOnly = true)
    public BusinessDto findById(UUID businessId) {
        return businessRepository.findById(businessId)
                .map(businessMapper::toBusinessDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public BusinessRegistrationResult registerBusiness(UUID ownerId,
                                                        String businessName,
                                                        UUID businessCategoryId,
                                                        String businessCategoryName,
                                                        BusinessScope businessScope,
                                                        String branchName,
                                                        UUID branchCityId,
                                                        String branchAddress,
                                                        Boolean onlineOnly,
                                                        String contactEmail,
                                                        String countryCode) {
        if (businessScope == null) {
            throw new ValidationException(ErrorCode.BUSINESS_SCOPE_REQUIRED);
        }
        Category category = businessCategoryId != null
                ? categoryService.requireActiveCategory(businessCategoryId, CategoryType.BUSINESS)
                : categoryService.resolveOrCreate(businessCategoryName, CategoryType.BUSINESS);
        Boolean computedOnlineOnly = branchName == null || branchName.isBlank() || Boolean.TRUE.equals(onlineOnly);
        Business business = businessRepository.save(businessMapper.toBusinessEntity(
                businessName, category, businessScope, countryCode, computedOnlineOnly));
        UUID businessId = business.getId();
        BusinessDto businessDto = businessMapper.toBusinessDto(business);

        BusinessBranchDto branchDto = null;
        if (branchName != null && !branchName.isBlank()) {
            branchDto = businessBranchService.create(
                    businessId, branchCityId, branchName.trim(), branchAddress, null,
                    null, null, null, null, null, false);
        }

        BusinessMemberDto memberDto = businessMemberService.createOwner(businessId, ownerId);
        businessProfileService.save(businessId, null, null, null, null, null, contactEmail,
                null, null, null, DeliveryCoverage.NO_DELIVERY, List.of(), false);

        return BusinessRegistrationResult.builder()
                .business(businessDto)
                .branch(branchDto)
                .member(memberDto)
                .build();
    }

    @Override
    @Transactional
    public BusinessRegistrationResult onboard(UUID ownerId,
                                              String businessName,
                                              UUID businessCategoryId,
                                              String businessCategoryName,
                                              BusinessScope businessScope,
                                              BusinessLegalForm legalForm,
                                              String legalIdentifier,
                                              String legalName,
                                              String countryCode,
                                              String contactEmail,
                                              DeliveryCoverage deliveryCoverage,
                                              List<String> deliveryCities,
                                              Boolean pickupAvailable,
                                              boolean hasPhysicalBranches) {
        if (businessScope == null) {
            throw new ValidationException(ErrorCode.BUSINESS_SCOPE_REQUIRED);
        }
        if (businessCategoryId == null && (businessCategoryName == null
                || businessCategoryName.isBlank())) {
            throw new ValidationException(ErrorCode.BUSINESS_ONBOARDING_INVALID);
        }
        if (legalForm != BusinessLegalForm.NONE && (legalIdentifier == null
                || legalIdentifier.isBlank())) {
            throw new ValidationException(ErrorCode.BUSINESS_ONBOARDING_INVALID);
        }
        Category category = businessCategoryId != null
                ? categoryService.requireActiveCategory(businessCategoryId, CategoryType.BUSINESS)
                : categoryService.resolveOrCreate(businessCategoryName, CategoryType.BUSINESS);
        String normalizedIdentifier = legalIdentifier == null || legalIdentifier.isBlank()
                ? null : legalIdentifier.trim();
        Business business = businessRepository.save(businessMapper.toBusinessEntity(
                businessName.trim(), category, businessScope, countryCode, !hasPhysicalBranches, legalForm,
                normalizedIdentifier, legalName == null || legalName.isBlank() ? null : legalName.trim()));
        UUID businessId = business.getId();
        BusinessMemberDto memberDto = businessMemberService.createOwner(businessId, ownerId);
        businessProfileService.save(businessId, null, null, null, null, null, contactEmail,
                null, null, null, deliveryCoverage, deliveryCities, pickupAvailable);
        return BusinessRegistrationResult.builder()
                .business(businessMapper.toBusinessDto(business))
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
