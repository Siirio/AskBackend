package kz.ask.platform.application;

import java.util.List;
import kz.ask.business.domain.enums.UniqueOfferStatus;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.business.infrastructure.repository.UniqueOfferRepository;
import kz.ask.item.domain.enums.ProductModerationStatus;
import kz.ask.item.infrastructure.repository.ProductRepository;
import kz.ask.chat.domain.enums.ConversationStatus;
import kz.ask.chat.domain.enums.ConversationType;
import kz.ask.chat.domain.repository.ChatConversationRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import kz.ask.moderation.infrastructure.repository.ContentReportRepository;
import kz.ask.platform.api.dto.PlatformDashboardResponse;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.service.infrastructure.repository.ServiceOfferingRepository;

import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformDashboardProcessor {

    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ContentReportRepository contentReportRepository;
    private final AppUserRepository appUserRepository;
    private final PlatformMembershipService platformMembershipService;

    @Transactional(readOnly = true)
    public PlatformDashboardResponse getDashboard(AskPrincipal principal) {
        if (platformMembershipService.findActiveByUser(principal.getUserId()) == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        List<UniqueOfferStatus> activeOfferStatuses = List.of(UniqueOfferStatus.ACTIVE, UniqueOfferStatus.UPCOMING);
        return PlatformDashboardResponse.builder()
                .totalBusinesses(businessRepository.count())
                .totalActiveProducts(productRepository.count())
                .totalActiveServices(serviceOfferingRepository.count())
                .totalActiveDrops(uniqueOfferRepository.countByStatusIn(activeOfferStatuses))
                .openSupportConversations(
                        chatConversationRepository.countByConversationStatusAndConversationType(
                                ConversationStatus.PENDING, ConversationType.PLATFORM_SUPPORT)
                        + chatConversationRepository.countByConversationStatusAndConversationType(
                                ConversationStatus.IN_CHAT, ConversationType.PLATFORM_SUPPORT))
                .pendingModerationItems(
                        contentReportRepository.countByStatus(ContentReportStatus.OPEN)
                        + productRepository.countByModerationStatus(ProductModerationStatus.PENDING))
                .totalUsers(appUserRepository.count())
                .build();
    }
}
