package kz.ask.business.infrastructure.mapper;

import java.time.Instant;
import java.util.List;
import kz.ask.business.api.dto.InviteResponse;
import kz.ask.business.api.dto.StaffResponse;
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
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public Business toBusinessEntity(String name) {
        Business business = new Business();
        business.setName(name);
        business.setStatus(RecordStatus.ACTIVE);
        return business;
    }

    public BusinessBranch toBranchEntity(Business business, City city, String name,
                                          String address, Boolean onlineOnly) {
        BusinessBranch branch = new BusinessBranch();
        branch.setBusiness(business);
        branch.setCity(city);
        branch.setName(name);
        branch.setAddress(address);
        branch.setOnlineOnly(onlineOnly);
        branch.setStatus(RecordStatus.ACTIVE);
        return branch;
    }

    public BusinessMember toBusinessMemberEntity(Business business, AppUser owner) {
        BusinessMember member = new BusinessMember();
        member.setBusiness(business);
        member.setUser(owner);
        member.setRole(BusinessMemberRole.OWNER);
        member.setStatus(RecordStatus.ACTIVE);
        return member;
    }

    public BusinessContact toContactEntity(Business business, BusinessBranch branch,
                                            ContactType type, String value) {
        BusinessContact contact = new BusinessContact();
        contact.setBusiness(business);
        contact.setBranch(branch);
        contact.setContactType(type);
        contact.setContactValue(value);
        contact.setPrimaryContact(true);
        contact.setStatus(RecordStatus.ACTIVE);
        return contact;
    }

    public BranchMember toBranchMemberEntity(BusinessBranch branch, AppUser user,
                                              BranchMemberRole role) {
        BranchMember member = new BranchMember();
        member.setBranch(branch);
        member.setUser(user);
        member.setRole(role);
        member.setStatus(RecordStatus.ACTIVE);
        return member;
    }

    public BranchInvite toInviteEntity(BusinessBranch branch, BranchMemberRole role,
                                        Integer maxUses, Instant expiresAt,
                                        AppUser createdBy, String code) {
        BranchInvite invite = new BranchInvite();
        invite.setBranch(branch);
        invite.setRole(role);
        invite.setMaxUses(maxUses);
        invite.setUseCount(0);
        invite.setExpiresAt(expiresAt);
        invite.setCreatedBy(createdBy);
        invite.setCode(code);
        return invite;
    }

    public StaffResponse toStaffResponse(BranchMember member, String decryptedTempPassword) {
        AppUser user = member.getUser();
        return StaffResponse.builder()
                .id(member.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(member.getRole().name())
                .status(user.getStatus().name())
                .tempPassword(decryptedTempPassword)
                .activatedAt(user.getActivatedAt())
                .build();
    }

    public List<StaffResponse> toStaffResponseList(List<BranchMember> members) {
        return members.stream().map(m -> toStaffResponse(m, null)).toList();
    }

    public InviteResponse toInviteResponse(BranchInvite invite) {
        return InviteResponse.builder()
                .id(invite.getId())
                .code(invite.getCode())
                .role(invite.getRole().name())
                .maxUses(invite.getMaxUses())
                .useCount(invite.getUseCount())
                .expiresAt(invite.getExpiresAt())
                .revokedAt(invite.getRevokedAt())
                .build();
    }

    public List<InviteResponse> toInviteResponseList(List<BranchInvite> invites) {
        return invites.stream().map(this::toInviteResponse).toList();
    }
}
