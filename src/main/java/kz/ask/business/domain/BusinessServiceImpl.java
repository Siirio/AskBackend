package kz.ask.business.domain;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BranchInvite;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.entity.City;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.infrastructure.mapper.BusinessMapper;
import kz.ask.business.infrastructure.repository.BranchInviteRepository;
import kz.ask.business.infrastructure.repository.BranchMemberRepository;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessContactRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.CityRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.enums.RecordStatus;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessContactRepository businessContactRepository;
    private final CityRepository cityRepository;
    private final BranchMemberRepository branchMemberRepository;
    private final BranchInviteRepository branchInviteRepository;
    private final BusinessMapper businessMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public BusinessRegistrationResult registerBusiness(AppUser owner,
                                                        String businessName,
                                                        String branchName,
                                                        UUID branchCityId,
                                                        String branchAddress,
                                                        Boolean onlineOnly,
                                                        String contactEmail,
                                                        String contactPhone) {
        Business business = businessRepository.save(businessMapper.toBusinessEntity(businessName));

        City city = null;
        if (branchCityId != null) {
            city = cityRepository.findById(branchCityId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.CITY_NOT_FOUND));
        }

        BusinessBranch branch = businessBranchRepository.save(
                businessMapper.toBranchEntity(business, city, branchName, branchAddress, onlineOnly));

        BusinessMember member = businessMemberRepository.save(
                businessMapper.toBusinessMemberEntity(business, owner));

        BusinessContact contact = null;
        if (contactEmail != null) {
            contact = businessContactRepository.save(
                    businessMapper.toContactEntity(business, branch, ContactType.EMAIL, contactEmail));
        } else if (contactPhone != null) {
            contact = businessContactRepository.save(
                    businessMapper.toContactEntity(business, branch, ContactType.PHONE, contactPhone));
        }

        return new BusinessRegistrationResult(business, branch, member, contact);
    }

    @Override
    public BusinessRegistrationResult findByOwner(UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        if (member == null) return null;
        return new BusinessRegistrationResult(
                member.getBusiness(),
                businessBranchRepository
                        .findByBusinessIdAndStatus(member.getBusiness().getId(), RecordStatus.ACTIVE)
                        .stream().findFirst().orElse(null),
                member,
                null);
    }

    @Override
    @Transactional
    public BranchMember addBranchMember(BusinessBranch branch, AppUser user, BranchMemberRole role) {
        return branchMemberRepository.save(businessMapper.toBranchMemberEntity(branch, user, role));
    }

    @Override
    public List<BranchMember> findBranchMembers(UUID branchId) {
        return branchMemberRepository.findByBranchIdAndStatus(branchId, RecordStatus.ACTIVE);
    }

    @Override
    public Boolean isOwnerOfBusiness(UUID businessId, UUID userId) {
        BusinessMember member = businessMemberRepository.findByUserIdAndRoleAndStatus(
                userId, BusinessMemberRole.OWNER, RecordStatus.ACTIVE);
        return member != null && member.getBusiness().getId().equals(businessId);
    }

    @Override
    public Boolean isOwnerOrManagerOfBranch(UUID branchId, UUID userId) {
        BusinessBranch branch = businessBranchRepository.findById(branchId).orElse(null);
        if (branch == null) return false;
        if (isOwnerOfBusiness(branch.getBusiness().getId(), userId)) return true;
        List<BranchMember> members = branchMemberRepository.findByUserIdAndStatus(userId, RecordStatus.ACTIVE);
        return members.stream().anyMatch(m -> m.getBranch().getId().equals(branchId)
                && (m.getRole() == BranchMemberRole.MANAGER || m.getRole() == BranchMemberRole.OPERATOR));
    }

    @Override
    public BusinessBranch findBranchById(UUID branchId) {
        return businessBranchRepository.findById(branchId).orElse(null);
    }

    @Override
    public BusinessBranch findBranchByBusinessAndId(UUID businessId, UUID branchId) {
        BusinessBranch branch = businessBranchRepository.findById(branchId).orElse(null);
        if (branch == null || !branch.getBusiness().getId().equals(businessId)) {
            return null;
        }
        return branch;
    }

    @Override
    @Transactional
    public BranchInvite createInvite(BusinessBranch branch, BranchMemberRole role, Integer maxUses,
                                      Long ttlSeconds, AppUser createdBy) {
        String code = generateInviteCode();
        return branchInviteRepository.save(
                businessMapper.toInviteEntity(branch, role, maxUses,
                        Instant.now().plusSeconds(ttlSeconds), createdBy, code));
    }

    @Override
    public List<BranchInvite> findBranchInvites(UUID branchId) {
        return branchInviteRepository.findByBranchIdAndRevokedAtIsNull(branchId);
    }

    @Override
    @Transactional
    public void revokeInvite(UUID inviteId) {
        branchInviteRepository.findById(inviteId).ifPresent(invite -> {
            invite.setRevokedAt(Instant.now());
        });
    }

    private String generateInviteCode() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
