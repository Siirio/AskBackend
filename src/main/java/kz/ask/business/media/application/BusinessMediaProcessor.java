package kz.ask.business.media.application;

import java.util.UUID;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.media.infrastructure.BusinessMediaStorage;
import kz.ask.business.profile.api.dto.BusinessProfileResponse;
import kz.ask.business.profile.application.BusinessProfileProcessor;
import kz.ask.business.profile.domain.BusinessProfileService;
import kz.ask.business.profile.domain.dto.BusinessProfileDto;
import kz.ask.business.uniqueoffer.api.dto.UniqueOfferResponse;
import kz.ask.business.uniqueoffer.domain.UniqueOfferService;
import kz.ask.business.uniqueoffer.domain.dto.UniqueOfferDto;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class BusinessMediaProcessor {

    private final BusinessService businessService;
    private final BusinessProfileService businessProfileService;
    private final BusinessProfileProcessor businessProfileProcessor;
    private final UniqueOfferService uniqueOfferService;
    private final BusinessMediaStorage businessMediaStorage;
    private final ManagedImageStorage imageStorage;

    public BusinessProfileResponse uploadProfileLogo(
            AskPrincipal principal,
            UUID businessId,
            MultipartFile file) {
        requireManager(principal, businessId);
        BusinessProfileDto current = businessProfileService.findByBusinessId(businessId);
        String previousUrl = current == null ? null : current.getLogoUrl();
        String storedName = imageStorage.store(file);
        String fileUrl = imageStorage.publicUrl(storedName);
        try {
            businessProfileService.save(
                    businessId, null, fileUrl, null, null, null, null, null, null, null,
                    null, null, null);
        } catch (RuntimeException e) {
            deleteManagedFile(fileUrl);
            throw e;
        }
        deleteManagedFile(previousUrl);
        return businessProfileProcessor.get(businessId);
    }

    public BusinessProfileResponse uploadProfileCover(
            AskPrincipal principal,
            UUID businessId,
            MultipartFile file) {
        requireManager(principal, businessId);
        BusinessProfileDto current = businessProfileService.findByBusinessId(businessId);
        String previousUrl = current == null ? null : current.getCoverUrl();
        String storedName = imageStorage.store(file);
        String fileUrl = imageStorage.publicUrl(storedName);
        try {
            businessProfileService.save(
                    businessId, null, null, fileUrl, null, null, null, null, null, null,
                    null, null, null);
        } catch (RuntimeException e) {
            deleteManagedFile(fileUrl);
            throw e;
        }
        deleteManagedFile(previousUrl);
        return businessProfileProcessor.get(businessId);
    }

    public UniqueOfferResponse uploadOfferCover(
            AskPrincipal principal,
            UUID offerId,
            MultipartFile file) {
        UniqueOfferDto current = uniqueOfferService.findById(offerId);
        requireOwner(principal, current.getBusinessId());
        String storedName = imageStorage.store(file);
        String fileUrl = imageStorage.publicUrl(storedName);
        UniqueOfferDto update = UniqueOfferDto.builder().coverUrl(fileUrl).build();
        UniqueOfferDto saved;
        try {
            saved = uniqueOfferService.update(offerId, update);
        } catch (RuntimeException e) {
            deleteManagedFile(fileUrl);
            throw e;
        }
        deleteManagedFile(current.getCoverUrl());
        return UniqueOfferResponse.builder()
                .id(saved.getId())
                .businessId(saved.getBusinessId())
                .name(saved.getName())
                .description(saved.getDescription())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .type(saved.getType())
                .status(saved.getStatus())
                .coverUrl(saved.getCoverUrl())
                .discountPercent(saved.getDiscountPercent())
                .discountAmount(saved.getDiscountAmount())
                .isActive(saved.getIsActive())
                .currency(saved.getCurrency())
                .tags(saved.getTags())
                .itemIds(saved.getItemIds())
                .serviceIds(saved.getServiceIds())
                .branchIds(saved.getBranchIds())
                .build();
    }

    public void deleteOffer(AskPrincipal principal, UUID offerId) {
        UniqueOfferDto current = uniqueOfferService.findById(offerId);
        requireOwner(principal, current.getBusinessId());
        uniqueOfferService.delete(offerId);
        deleteManagedFile(current.getCoverUrl());
    }

    public Resource resolve(String storedName) {
        Resource resource = businessMediaStorage.resolve(storedName);
        if (resource == null) {
            throw new NotFoundException(ErrorCode.BUSINESS_MEDIA_NOT_FOUND);
        }
        return resource;
    }

    private void requireManager(AskPrincipal principal, UUID businessId) {
        if (!businessService.isManagerOrAboveOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void requireOwner(AskPrincipal principal, UUID businessId) {
        if (!businessService.isOwnerOfBusiness(businessId, principal.getUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void deleteManagedFile(String fileUrl) {
        String storedName = imageStorage.storedName(fileUrl);
        if (storedName != null) {
            imageStorage.delete(storedName);
        }
    }
}
