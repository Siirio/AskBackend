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
import kz.ask.business.domain.entity.BrandDrop;
import kz.ask.business.domain.entity.BrandPageBlock;
import kz.ask.business.domain.entity.BrandProfile;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.entity.City;
import kz.ask.business.domain.entity.DataSource;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.domain.enums.BrandDropStatus;
import kz.ask.business.domain.enums.BrandDropType;
import kz.ask.business.domain.enums.BrandPageBlockType;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.domain.dto.BrandDropDto;
import kz.ask.business.domain.dto.BrandPageBlockDto;
import kz.ask.business.domain.dto.BrandProfileDto;
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

    public BrandProfile toBrandProfileEntity(Business business) {
        BrandProfile profile = new BrandProfile();
        profile.setBusiness(business);
        return profile;
    }

    public void updateBrandProfile(BrandProfile profile, String brandColor, String logoUrl, String coverUrl,
                                    String toneOfVoice, String description, String instagramUrl,
                                    String telegramUrl, String websiteUrl) {
        profile.setBrandColor(brandColor);
        profile.setLogoUrl(logoUrl);
        profile.setCoverUrl(coverUrl);
        profile.setToneOfVoice(toneOfVoice);
        profile.setDescription(description);
        profile.setInstagramUrl(instagramUrl);
        profile.setTelegramUrl(telegramUrl);
        profile.setWebsiteUrl(websiteUrl);
    }

    public BrandPageBlock toBrandPageBlockEntity(Business business, BrandPageBlockType blockType,
                                                  Integer displayOrder, String configJson, Boolean enabled) {
        BrandPageBlock block = new BrandPageBlock();
        block.setBusiness(business);
        block.setBlockType(blockType);
        block.setDisplayOrder(displayOrder);
        block.setConfigJson(configJson);
        block.setEnabled(enabled);
        return block;
    }

    public BrandDrop toBrandDropEntity(Business business, String name, String description,
                                        java.time.Instant startDate, java.time.Instant endDate,
                                        BrandDropType type, BrandDropStatus status, String coverUrl) {
        BrandDrop drop = new BrandDrop();
        drop.setBusiness(business);
        drop.setName(name);
        drop.setDescription(description);
        drop.setStartDate(startDate);
        drop.setEndDate(endDate);
        drop.setType(type);
        drop.setStatus(status);
        drop.setCoverUrl(coverUrl);
        return drop;
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

    public BrandProfileDto toBrandProfileDto(BrandProfile entity) {
        Business business = entity.getBusiness();
        return BrandProfileDto.builder()
                .id(entity.getId())
                .businessId(business.getId())
                .businessName(business.getName())
                .brandColor(entity.getBrandColor())
                .logoUrl(entity.getLogoUrl())
                .coverUrl(entity.getCoverUrl())
                .toneOfVoice(entity.getToneOfVoice())
                .description(entity.getDescription())
                .instagramUrl(entity.getInstagramUrl())
                .telegramUrl(entity.getTelegramUrl())
                .websiteUrl(entity.getWebsiteUrl())
                .build();
    }

    public BrandPageBlockDto toBrandPageBlockDto(BrandPageBlock entity) {
        return BrandPageBlockDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .blockType(entity.getBlockType().name())
                .displayOrder(entity.getDisplayOrder())
                .configJson(entity.getConfigJson())
                .enabled(entity.getEnabled())
                .build();
    }

    public BrandDropDto toBrandDropDto(BrandDrop entity) {
        return BrandDropDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .type(entity.getType().name())
                .status(entity.getStatus().name())
                .coverUrl(entity.getCoverUrl())
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
