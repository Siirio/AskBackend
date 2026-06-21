package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BranchInvite;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.identity.domain.entity.AppUser;

public interface BusinessService {

    BusinessRegistrationResult registerBusiness(AppUser owner,
                                                String businessName,
                                                String branchName,
                                                UUID branchCityId,
                                                String branchAddress,
                                                Boolean onlineOnly,
                                                String contactEmail,
                                                String contactPhone);

    BusinessRegistrationResult findByOwner(UUID userId);

    BranchMember addBranchMember(BusinessBranch branch, AppUser user, BranchMemberRole role);

    List<BranchMember> findBranchMembers(UUID branchId);

    Boolean isOwnerOfBusiness(UUID businessId, UUID userId);

    Boolean isOwnerOrManagerOfBranch(UUID branchId, UUID userId);

    BusinessBranch findBranchById(UUID branchId);

    BranchInvite createInvite(BusinessBranch branch, BranchMemberRole role, Integer maxUses, Long ttlSeconds, AppUser createdBy);

    List<BranchInvite> findBranchInvites(UUID branchId);

    void revokeInvite(UUID inviteId);

    record BusinessRegistrationResult(
            kz.ask.business.domain.entity.Business business,
            kz.ask.business.domain.entity.BusinessBranch branch,
            kz.ask.business.domain.entity.BusinessMember member,
            kz.ask.business.domain.entity.BusinessContact contact
    ) {}
}
