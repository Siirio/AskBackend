package kz.ask.platform.application;

import java.util.List;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.uniqueoffer.domain.enums.UniqueOfferStatus;
import kz.ask.business.uniqueoffer.infrastructure.repository.UniqueOfferRepository;
import kz.ask.chat.domain.enums.ConversationStatus;
import kz.ask.chat.domain.repository.ChatConversationRepository;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.infrastructure.repository.ServiceOfferingRepository;
import kz.ask.platform.api.dto.PlatformDashboardResponse;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PlatformDashboardProcessor {

    private final PlatformMembershipService platformMembershipService;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final UniqueOfferRepository uniqueOfferRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public PlatformDashboardResponse get(AskPrincipal principal) {
        if (platformMembershipService.findActiveByUser(principal.getUserId()) == null) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return PlatformDashboardResponse.builder()
                .totalBusinesses(businessRepository.count())
                .totalActiveProducts(productRepository.countByIsActive(Boolean.TRUE))
                .totalActiveServices(serviceOfferingRepository.countByIsActive(Boolean.TRUE))
                .totalActiveDrops(uniqueOfferRepository.countByStatusIn(List.of(UniqueOfferStatus.ACTIVE)))
                .openSupportConversations(chatConversationRepository.countByConversationStatusIn(
                        List.of(ConversationStatus.PENDING, ConversationStatus.IN_CHAT)))
                .pendingModerationItems(
                        productRepository.countByModerationStatus(ProductModerationStatus.PENDING))
                .totalUsers(appUserRepository.count())
                .build();
    }
}
