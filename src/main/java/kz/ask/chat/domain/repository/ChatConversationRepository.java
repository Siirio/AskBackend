package kz.ask.chat.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.chat.domain.entity.ChatConversation;
import kz.ask.chat.domain.enums.ConversationStatus;
import kz.ask.chat.domain.enums.ConversationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    @Query("SELECT c FROM ChatConversation c WHERE c.businessId = :businessId ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<ChatConversation> findActiveByBusinessId(@Param("businessId") UUID businessId, Pageable pageable);

    @Query("SELECT c FROM ChatConversation c WHERE c.customerId = :customerId ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<ChatConversation> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    List<ChatConversation> findAllByCustomerId(UUID customerId);

    Optional<ChatConversation> findFirstByCustomerIdAndBusinessIdIsNullAndConversationTypeOrderByCreatedAtDesc(
            UUID customerId, ConversationType conversationType);

    Optional<ChatConversation> findFirstByCustomerIdAndBusinessIdAndConversationTypeOrderByCreatedAtDesc(
            UUID customerId, UUID businessId, ConversationType conversationType);

    @Query("SELECT c FROM ChatConversation c WHERE c.businessId = :businessId ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<ChatConversation> findAllByBusinessId(@Param("businessId") UUID businessId, Pageable pageable);

    @Query("SELECT c FROM ChatConversation c WHERE c.conversationType = :type ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<ChatConversation> findByConversationType(@Param("type") ConversationType type, Pageable pageable);

    @Query("SELECT c FROM ChatConversation c WHERE c.conversationType IN (:generalType, :supportType, :managedImportType) ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<ChatConversation> findPlatformConversations(
            @Param("generalType") ConversationType generalType,
            @Param("supportType") ConversationType supportType,
            @Param("managedImportType") ConversationType managedImportType,
            Pageable pageable);

    Long countByConversationStatusAndConversationType(ConversationStatus status, ConversationType type);
}
