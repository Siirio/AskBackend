package kz.ask.business.infrastructure.mapper;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import kz.ask.business.domain.dto.BranchInviteDto;
import kz.ask.business.domain.dto.BranchMemberDto;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.dto.BusinessContactDto;
import kz.ask.business.domain.dto.BusinessDto;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.dto.CityDto;
import kz.ask.business.domain.dto.DataSourceDto;
import kz.ask.business.domain.entity.BranchInvite;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.entity.City;
import kz.ask.business.domain.entity.DataSource;
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

    public BusinessDto toBusinessDto(Business entity) {
        return BusinessDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public BusinessBranchDto toBusinessBranchDto(BusinessBranch entity) {
        City city = entity.getCity();
        return BusinessBranchDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : null)
                .name(entity.getName())
                .address(entity.getAddress())
                .onlineOnly(entity.getOnlineOnly())
                .status(entity.getStatus().name())
                .build();
    }

    public BusinessMemberDto toBusinessMemberDto(BusinessMember entity) {
        return BusinessMemberDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .role(entity.getRole().name())
                .build();
    }

    public BusinessContactDto toBusinessContactDto(BusinessContact entity) {
        return BusinessContactDto.builder()
                .id(entity.getId())
                .build();
    }

    public BranchMemberDto toBranchMemberDto(BranchMember entity) {
        AppUser user = entity.getUser();
        return BranchMemberDto.builder()
                .id(entity.getId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userDisplayName(user.getDisplayName())
                .userStatus(user.getStatus().name())
                .role(entity.getRole().name())
                .userActivatedAt(user.getActivatedAt())
                .build();
    }

    public List<BranchMemberDto> toBranchMemberDtoList(List<BranchMember> entities) {
        return entities.stream().map(this::toBranchMemberDto).collect(Collectors.toList());
    }

    public BranchInviteDto toBranchInviteDto(BranchInvite entity) {
        return BranchInviteDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .role(entity.getRole().name())
                .maxUses(entity.getMaxUses())
                .useCount(entity.getUseCount())
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .build();
    }

    public List<BranchInviteDto> toBranchInviteDtoList(List<BranchInvite> entities) {
        return entities.stream().map(this::toBranchInviteDto).collect(Collectors.toList());
    }

    public DataSourceDto toDataSourceDto(DataSource entity) {
        return DataSourceDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .sourceType(entity.getSourceType().name())
                .name(entity.getName())
                .build();
    }

    public CityDto toCityDto(City entity) {
        return CityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
