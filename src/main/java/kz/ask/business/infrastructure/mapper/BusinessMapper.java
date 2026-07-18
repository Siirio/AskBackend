package kz.ask.business.infrastructure.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import kz.ask.business.domain.dto.BranchMemberDto;
import kz.ask.business.domain.dto.BusinessBranchDto;
import kz.ask.business.domain.dto.BusinessContactDto;
import kz.ask.business.domain.dto.BusinessDto;
import kz.ask.business.domain.dto.BusinessMemberDto;
import kz.ask.business.domain.dto.CityDto;
import kz.ask.business.domain.dto.DataSourceDto;
import kz.ask.business.domain.entity.UniqueOffer;
import kz.ask.business.domain.entity.BrandProfile;
import kz.ask.business.domain.entity.BranchMember;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.domain.entity.BusinessContact;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.entity.City;
import kz.ask.business.domain.entity.DataSource;
import kz.ask.business.domain.enums.BranchMemberRole;
import kz.ask.business.domain.enums.UniqueOfferStatus;
import kz.ask.business.domain.enums.UniqueOfferType;
import kz.ask.business.domain.enums.BusinessMemberRole;
import kz.ask.business.domain.enums.ContactVisibility;
import kz.ask.business.domain.enums.ContactType;
import kz.ask.business.domain.dto.UniqueOfferDto;
import kz.ask.business.domain.dto.BrandProfileDto;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public Business toBusinessEntity(String name) {
        Business business = new Business();
        business.setName(name);
        business.setCurrency("KZT");
        business.setStatus(RecordStatus.ACTIVE);
        return business;
    }

    public BusinessBranch toBranchEntity(Business business, City city, String name,
                                          String address, String addressDetails, Boolean onlineOnly,
                                          BigDecimal latitude, BigDecimal longitude) {
        BusinessBranch branch = new BusinessBranch();
        branch.setBusiness(business);
        branch.setCity(city);
        branch.setName(name);
        branch.setAddress(address);
        branch.setAddressDetails(addressDetails);
        branch.setOnlineOnly(onlineOnly);
        branch.setLatitude(latitude);
        branch.setLongitude(longitude);
        branch.setStatus(RecordStatus.ACTIVE);
        return branch;
    }

    public BusinessMember toBusinessMemberEntity(Business business, AppUser user, BusinessMemberRole role) {
        BusinessMember member = new BusinessMember();
        member.setBusiness(business);
        member.setUser(user);
        member.setRole(role);
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
        contact.setDisplayValue(value);
        contact.setVisibility(ContactVisibility.AFTER_CONTACT);
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

    public UniqueOffer toUniqueOfferEntity(Business business, String name, String description,
                                             java.time.Instant startDate, java.time.Instant endDate,
                                             UniqueOfferType type, UniqueOfferStatus status, String coverUrl,
                                             List<String> tags) {
        UniqueOffer offer = new UniqueOffer();
        offer.setBusiness(business);
        offer.setName(name);
        offer.setDescription(description);
        offer.setStartDate(startDate);
        offer.setEndDate(endDate);
        offer.setType(type);
        offer.setStatus(status);
        offer.setCoverUrl(coverUrl);
        offer.setTags(tags == null ? List.of() : tags);
        offer.setCurrency("KZT");
        return offer;
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

    public BusinessBranchDto toBusinessBranchDto(BusinessBranch entity) {
        City city = entity.getCity();
        return BusinessBranchDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .cityId(city != null ? city.getId() : null)
                .cityName(city != null ? city.getName() : null)
                .name(entity.getName())
                .address(entity.getAddress())
                .addressDetails(entity.getAddressDetails())
                .onlineOnly(entity.getOnlineOnly())
                .status(entity.getStatus().name())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    public UniqueOfferDto toUniqueOfferDto(UniqueOffer entity) {
        return UniqueOfferDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .type(entity.getType().name())
                .status(entity.getStatus().name())
                .coverUrl(entity.getCoverUrl())
                .discountPercent(entity.getDiscountPercent())
                .discountAmount(entity.getDiscountAmount())
                .enabled(entity.getEnabled())
                .currency(entity.getCurrency())
                .tags(entity.getTags())
                .build();
    }

    public BusinessMemberDto toBusinessMemberDto(BusinessMember entity) {
        return BusinessMemberDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .businessName(entity.getBusiness().getName())
                .userId(entity.getUser().getId())
                .email(entity.getUser().getEmail())
                .displayName(entity.getUser().getDisplayName())
                .role(entity.getRole().name())
                .status(entity.getStatus().name())
                .build();
    }

    public BusinessContactDto toBusinessContactDto(BusinessContact entity) {
        return BusinessContactDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .contactType(entity.getContactType().name())
                .displayValue(entity.getDisplayValue())
                .visibility(entity.getVisibility().name())
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
                .branchName(entity.getBranch().getName())
                .userActivatedAt(user.getActivatedAt())
                .build();
    }

    public List<BranchMemberDto> toBranchMemberDtoList(List<BranchMember> entities) {
        return entities.stream().map(this::toBranchMemberDto).collect(Collectors.toList());
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
