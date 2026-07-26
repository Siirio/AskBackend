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
                                       String instagramUrl, String telegramUrl, String websiteUrl) {
        profile.setBrandColor(brandColor);
        profile.setLogoUrl(logoUrl);
        profile.setCoverUrl(coverUrl);
        profile.setDescription(description);
        profile.setNumber(number);
        profile.setEmail(email);
        profile.setInstagramUrl(instagramUrl);
        profile.setTelegramUrl(telegramUrl);
        profile.setWebsiteUrl(websiteUrl);
    }

    public UniqueOffer toUniqueOfferEntity(Business business, String name, String description,
                                             java.time.Instant startDate, java.time.Instant endDate,
                                             UniqueOfferType type, UniqueOfferStatus status, String coverUrl,
                                             Integer discountPercent, BigDecimal discountAmount,
                                             Boolean isActive, String currency, List<String> tags) {
        UniqueOffer offer = new UniqueOffer();
        offer.setBusiness(business);
        offer.setName(name);
        offer.setDescription(description);
        offer.setStartDate(startDate);
        offer.setEndDate(endDate);
        offer.setType(type);
        offer.setStatus(status);
        offer.setCoverUrl(coverUrl);
        offer.setDiscountPercent(discountPercent);
        offer.setDiscountAmount(discountAmount);
        offer.setTags(tags == null ? List.of() : tags);
        offer.setIsActive(isActive != null ? isActive : Boolean.TRUE);
        offer.setCurrency(currency != null ? currency : "KZT");
        return offer;
    }

    public void updateUniqueOffer(UniqueOffer offer, String name, String description,
                                   java.time.Instant startDate, java.time.Instant endDate,
                                   String type, String status, String coverUrl,
                                   Integer discountPercent, BigDecimal discountAmount,
                                   Boolean isActive, String currency, List<String> tags) {
        if (name != null) offer.setName(name);
        if (description != null) offer.setDescription(description);
        if (startDate != null) offer.setStartDate(startDate);
        if (endDate != null) offer.setEndDate(endDate);
        if (type != null) offer.setType(UniqueOfferType.valueOf(type));
        if (status != null) offer.setStatus(UniqueOfferStatus.valueOf(status));
        if (coverUrl != null) offer.setCoverUrl(coverUrl);
        if (discountPercent != null) offer.setDiscountPercent(discountPercent);
        if (discountAmount != null) offer.setDiscountAmount(discountAmount);
        if (isActive != null) offer.setIsActive(isActive);
        if (currency != null) offer.setCurrency(currency);
        if (tags != null) offer.setTags(tags);
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
