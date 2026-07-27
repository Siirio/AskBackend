package kz.ask.business.core.infrastructure.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import kz.ask.business.member.domain.dto.BranchMemberDto;
import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.branch.domain.dto.SpecialOpeningIntervalDto;
import kz.ask.business.branch.domain.dto.WeeklyOpeningIntervalDto;
import kz.ask.business.core.domain.dto.BusinessDto;
import kz.ask.business.member.domain.dto.BusinessMemberDto;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.shared.domain.dto.CityDto;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
import kz.ask.business.member.domain.entity.BranchMember;
import kz.ask.business.core.domain.entity.Business;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.enums.BusinessLegalForm;
import kz.ask.business.branch.domain.entity.BusinessBranch;
import kz.ask.business.branch.domain.entity.SpecialOpeningInterval;
import kz.ask.business.branch.domain.entity.WeeklyOpeningInterval;
import kz.ask.business.member.domain.entity.BusinessMember;
import kz.ask.business.profile.domain.entity.BusinessProfile;
import kz.ask.business.profile.domain.enums.DeliveryCoverage;
import kz.ask.shared.domain.entity.City;
import kz.ask.business.uniqueoffer.domain.entity.UniqueOffer;
import kz.ask.identity.authorization.domain.enums.Role;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferType;
import kz.ask.identity.domain.entity.AppUser;

import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public Business toBusinessEntity(String name, Category category, BusinessScope scope, String countryCode,
                                     Boolean onlineOnly) {
        return toBusinessEntity(name, category, scope, countryCode, onlineOnly,
                null, null, null);
    }

    public Business toBusinessEntity(String name, Category category, BusinessScope scope, String countryCode,
                                     Boolean onlineOnly, BusinessLegalForm legalForm,
                                     String legalIdentifier, String legalName) {
        Business business = new Business();
        business.setName(name);
        business.setCurrency("KZT");
        business.setCategory(category);
        business.setScope(scope);
        business.setCountryCode(countryCode);
        business.setOnlineOnly(Boolean.TRUE.equals(onlineOnly));
        business.setLegalForm(legalForm);
        business.setLegalIdentifier(legalIdentifier);
        business.setLegalName(legalName);
        business.setIin(legalForm == BusinessLegalForm.KZ_IP ? legalIdentifier : null);
        business.setBin(legalForm == BusinessLegalForm.KZ_TOO ? legalIdentifier : null);
        return business;
    }

    public BusinessBranch toBranchEntity(Business business, City city, String name,
                                          String address, String addressDetails,
                                          BigDecimal latitude, BigDecimal longitude,
                                          String timeZoneId) {
        BusinessBranch branch = new BusinessBranch();
        branch.setBusiness(business);
        branch.setCity(city);
        branch.setName(name);
        branch.setAddress(address);
        branch.setAddressDetails(addressDetails);
        branch.setLatitude(latitude);
        branch.setLongitude(longitude);
        branch.setTimeZoneId(timeZoneId);
        return branch;
    }

    public BusinessMember toBusinessMemberEntity(Business business, AppUser user, Role role) {
        BusinessMember member = new BusinessMember();
        member.setBusiness(business);
        member.setUser(user);
        member.setRole(role);
        return member;
    }

    public BusinessProfile toBusinessProfileEntity(Business business) {
        BusinessProfile profile = new BusinessProfile();
        profile.setBusiness(business);
        return profile;
    }

    public void updateBusinessProfile(BusinessProfile profile, String brandColor, String logoUrl,
                                       String coverUrl, String description, String number, String email,
                                       String instagramUrl, String telegramUrl, String websiteUrl,
                                       DeliveryCoverage deliveryCoverage, List<String> deliveryCities,
                                       Boolean pickupAvailable) {
        if (brandColor != null) profile.setBrandColor(brandColor);
        if (logoUrl != null) profile.setLogoUrl(logoUrl);
        if (coverUrl != null) profile.setCoverUrl(coverUrl);
        if (description != null) profile.setDescription(description);
        if (number != null) profile.setNumber(number);
        if (email != null) profile.setEmail(email);
        if (instagramUrl != null) profile.setInstagramUrl(instagramUrl);
        if (telegramUrl != null) profile.setTelegramUrl(telegramUrl);
        if (websiteUrl != null) profile.setWebsiteUrl(websiteUrl);
        if (deliveryCoverage != null) profile.setDeliveryCoverage(deliveryCoverage);
        if (deliveryCities != null) {
            profile.setDeliveryCities(deliveryCities.stream()
                    .filter(city -> city != null && !city.isBlank())
                    .map(String::trim)
                    .distinct()
                    .toList());
        }
        if (pickupAvailable != null) profile.setPickupAvailable(pickupAvailable);
    }

    public UniqueOffer toUniqueOfferEntity(Business business, UniqueOfferDto dto) {
        UniqueOffer offer = new UniqueOffer();
        offer.setBusiness(business);
        offer.setName(dto.getName());
        offer.setDescription(dto.getDescription());
        offer.setStartDate(dto.getStartDate());
        offer.setEndDate(dto.getEndDate());
        offer.setType(UniqueOfferType.valueOf(dto.getType()));
        offer.setStatus(UniqueOfferStatus.valueOf(dto.getStatus()));
        offer.setCoverUrl(dto.getCoverUrl());
        offer.setDiscountPercent(dto.getDiscountPercent());
        offer.setDiscountAmount(dto.getDiscountAmount());
        offer.setTags(dto.getTags() == null ? List.of() : dto.getTags());
        offer.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        offer.setCurrency(dto.getCurrency());
        offer.setItemIds(dto.getItemIds());
        offer.setServiceIds(dto.getServiceIds());
        offer.setBranchIds(dto.getBranchIds());
        return offer;
    }

    public void updateUniqueOffer(UniqueOffer offer, UniqueOfferDto dto) {
        if (dto.getName() != null) offer.setName(dto.getName());
        if (dto.getDescription() != null) offer.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) offer.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) offer.setEndDate(dto.getEndDate());
        if (dto.getType() != null) offer.setType(UniqueOfferType.valueOf(dto.getType()));
        if (dto.getStatus() != null) offer.setStatus(UniqueOfferStatus.valueOf(dto.getStatus()));
        if (dto.getCoverUrl() != null) offer.setCoverUrl(dto.getCoverUrl());
        if (dto.getDiscountPercent() != null) offer.setDiscountPercent(dto.getDiscountPercent());
        if (dto.getDiscountAmount() != null) offer.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getIsActive() != null) offer.setIsActive(dto.getIsActive());
        if (dto.getCurrency() != null) offer.setCurrency(dto.getCurrency());
        if (dto.getTags() != null) offer.setTags(dto.getTags());
        if (dto.getItemIds() != null) offer.setItemIds(dto.getItemIds());
        if (dto.getServiceIds() != null) offer.setServiceIds(dto.getServiceIds());
        if (dto.getBranchIds() != null) offer.setBranchIds(dto.getBranchIds());
    }

    public BranchMember toBranchMemberEntity(BusinessBranch branch, AppUser user,
                                              Role role) {
        BranchMember member = new BranchMember();
        member.setBranch(branch);
        member.setUser(user);
        member.setRole(role);
        return member;
    }

    public BusinessDto toBusinessDto(Business entity) {
        return BusinessDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .categoryId(entity.getCategory().getId())
                .categoryName(entity.getCategory().getName())
                .currency(entity.getCurrency())
                .scope(entity.getScope())
                .onlineOnly(entity.getOnlineOnly())
                .build();
    }

    public BusinessProfileDto toBusinessProfileDto(BusinessProfile entity) {
        Business business = entity.getBusiness();
        return BusinessProfileDto.builder()
                .id(entity.getId())
                .businessId(business.getId())
                .businessName(business.getName())
                .brandColor(entity.getBrandColor())
                .logoUrl(entity.getLogoUrl())
                .coverUrl(entity.getCoverUrl())
                .description(entity.getDescription())
                .number(entity.getNumber())
                .email(entity.getEmail())
                .instagramUrl(entity.getInstagramUrl())
                .telegramUrl(entity.getTelegramUrl())
                .websiteUrl(entity.getWebsiteUrl())
                .deliveryCoverage(entity.getDeliveryCoverage())
                .deliveryCities(entity.getDeliveryCities())
                .pickupAvailable(entity.getPickupAvailable())
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
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .timeZoneId(entity.getTimeZoneId())
                .pickupAvailable(entity.getPickupAvailable())
                .weeklyHours(entity.getWeeklyHours() != null
                        ? entity.getWeeklyHours().stream().map(this::toWeeklyDto).toList()
                        : null)
                .specialHours(entity.getSpecialHours() != null
                        ? entity.getSpecialHours().stream().map(this::toSpecialDto).toList()
                        : null)
                .build();
    }

    private WeeklyOpeningIntervalDto toWeeklyDto(WeeklyOpeningInterval entity) {
        return WeeklyOpeningIntervalDto.builder()
                .dayOfWeek(entity.getDayOfWeek())
                .opensAt(entity.getOpensAt())
                .closesAt(entity.getClosesAt())
                .build();
    }

    private SpecialOpeningIntervalDto toSpecialDto(SpecialOpeningInterval entity) {
        return SpecialOpeningIntervalDto.builder()
                .date(entity.getDate())
                .closed(entity.getClosed())
                .opensAt(entity.getOpensAt())
                .closesAt(entity.getClosesAt())
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
                .isActive(entity.getIsActive())
                .currency(entity.getCurrency())
                .tags(entity.getTags())
                .itemIds(entity.getItemIds())
                .serviceIds(entity.getServiceIds())
                .branchIds(entity.getBranchIds())
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

    public CityDto toCityDto(City entity) {
        return CityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
