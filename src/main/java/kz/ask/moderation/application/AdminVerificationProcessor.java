package kz.ask.moderation.application;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessVerification;
import kz.ask.business.domain.enums.VerificationStatus;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.BusinessVerificationRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.api.dto.ReviewVerificationRequest;
import kz.ask.moderation.api.dto.VerificationDetailResponse;
import kz.ask.moderation.api.dto.VerificationDetailResponse.VerificationHistoryItem;
import kz.ask.moderation.api.dto.VerificationListResponse;
import kz.ask.moderation.domain.entity.BusinessVerificationHistory;
import kz.ask.moderation.infrastructure.repository.BusinessVerificationHistoryRepository;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.search.domain.SearchVisibilityService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class AdminVerificationProcessor {

    private final BusinessVerificationRepository verificationRepository;
    private final BusinessVerificationHistoryRepository historyRepository;
    private final BusinessRepository businessRepository;
    private final PlatformMembershipService platformMembershipService;
    private final SearchVisibilityService searchVisibilityService;

    @Transactional(readOnly = true)
    public List<VerificationListResponse> listPending(AskPrincipal principal) {
        requirePermission(principal);
        List<BusinessVerification> verifications = verificationRepository
                .findByStatusOrderByCreatedAtAsc(VerificationStatus.PENDING);
        return verifications.stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VerificationDetailResponse getDetail(AskPrincipal principal, UUID businessId) {
        requirePermission(principal);
        BusinessVerification verification = verificationRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.BUSINESS_VERIFICATION_NOT_FOUND, businessId));
        List<BusinessVerificationHistory> history = historyRepository
                .findByBusinessIdOrderByCreatedAtDesc(businessId);
        return toDetailResponse(verification, history);
    }

    @Transactional
    public VerificationDetailResponse review(AskPrincipal principal, UUID businessId, ReviewVerificationRequest req) {
        requirePermission(principal);
        if (req.getStatus() == VerificationStatus.PENDING) {
            throw new NotFoundException(ErrorCode.BUSINESS_VERIFICATION_NOT_FOUND, businessId);
        }
        BusinessVerification verification = verificationRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.BUSINESS_VERIFICATION_NOT_FOUND, businessId));
        VerificationStatus fromStatus = verification.getStatus();
        verification.setStatus(req.getStatus());

        BusinessVerificationHistory history = new BusinessVerificationHistory();
        history.setBusiness(businessRepository.getReferenceById(businessId));
        history.setFromStatus(fromStatus);
        history.setToStatus(req.getStatus());
        history.setEditedBy(principal.getUserId());
        history.setComment(req.getComment());
        historyRepository.save(history);

        searchVisibilityService.republishBusinessOffers(businessId);
        List<BusinessVerificationHistory> allHistory = historyRepository
                .findByBusinessIdOrderByCreatedAtDesc(businessId);
        return toDetailResponse(verification, allHistory);
    }

    private void requirePermission(AskPrincipal principal) {
        PlatformMembershipDto membership = platformMembershipService.findActiveByUser(principal.getUserId());
        if (membership == null || !membership.getPermissions().contains(PlatformPermission.MODERATE_CONTENT)) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
    }

    private VerificationListResponse toListResponse(BusinessVerification verification) {
        return VerificationListResponse.builder()
                .businessId(verification.getBusiness().getId())
                .businessName(verification.getBusiness().getName())
                .status(verification.getStatus())
                .createdAt(verification.getCreatedAt())
                .updatedAt(verification.getUpdatedAt())
                .build();
    }

    private VerificationDetailResponse toDetailResponse(BusinessVerification verification,
                                                         List<BusinessVerificationHistory> history) {
        return VerificationDetailResponse.builder()
                .businessId(verification.getBusiness().getId())
                .businessName(verification.getBusiness().getName())
                .status(verification.getStatus())
                .binIin(verification.getBinIin())
                .twoGisUrl(verification.getTwoGisUrl())
                .kaspiUrl(verification.getKaspiUrl())
                .ozonUrl(verification.getOzonUrl())
                .wildberriesUrl(verification.getWildberriesUrl())
                .websiteUrl(verification.getWebsiteUrl())
                .instagramUrl(verification.getInstagramUrl())
                .telegramUrl(verification.getTelegramUrl())
                .phone(verification.getPhone())
                .corporateEmail(verification.getCorporateEmail())
                .createdAt(verification.getCreatedAt())
                .updatedAt(verification.getUpdatedAt())
                .history(history.stream()
                        .map(this::toHistoryItem)
                        .toList())
                .build();
    }

    private VerificationHistoryItem toHistoryItem(BusinessVerificationHistory h) {
        return VerificationHistoryItem.builder()
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .editedBy(h.getEditedBy())
                .comment(h.getComment())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
