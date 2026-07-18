package kz.ask.chat.application;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kz.ask.business.domain.BusinessMemberService;
import kz.ask.chat.api.dto.ChatAttachmentDto;
import kz.ask.chat.api.dto.ChatConversationDto;
import kz.ask.chat.api.dto.ChatFileDownloadDto;
import kz.ask.chat.api.dto.ChatFileUploadResponse;
import kz.ask.chat.domain.ChatService;
import kz.ask.chat.infrastructure.ChatFileStorage;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.platform.domain.PlatformMembershipService;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.enums.PlatformPermission;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ChatFileProcessor {

    private static final String FILE_URL_PREFIX = "/api/v1/chat/files/";

    private final ChatService chatService;
    private final ChatFileStorage chatFileStorage;
    private final BusinessMemberService businessMemberService;
    private final PlatformMembershipService platformMembershipService;
    private final ManagedImportService managedImportService;
    private final Long maxFileSize;
    private final Set<String> allowedExtensions;
    private final Set<String> allowedContentTypes;

    public ChatFileProcessor(
            ChatService chatService,
            ChatFileStorage chatFileStorage,
            BusinessMemberService businessMemberService,
            PlatformMembershipService platformMembershipService,
            ManagedImportService managedImportService,
            @Value("${ask.chat.max-file-size:10485760}") Long maxFileSize,
            @Value("${ask.chat.allowed-extensions:png,jpg,jpeg,pdf,txt,md,csv,xlsx}") String allowedExtensions,
            @Value("${ask.chat.allowed-content-types:image/png,image/jpeg,application/pdf,text/plain,text/markdown,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet}") String allowedContentTypes) {
        this.chatService = chatService;
        this.chatFileStorage = chatFileStorage;
        this.businessMemberService = businessMemberService;
        this.platformMembershipService = platformMembershipService;
        this.managedImportService = managedImportService;
        this.maxFileSize = maxFileSize;
        this.allowedExtensions = splitConfig(allowedExtensions);
        this.allowedContentTypes = splitConfig(allowedContentTypes);
    }

    public ChatFileUploadResponse upload(AskPrincipal principal, UUID conversationId, MultipartFile file) {
        requireParticipant(principal, conversationId);
        String originalName = file.getOriginalFilename();
        String extension = extension(originalName);
        String contentType = file.getContentType();
        if (file.isEmpty()
                || file.getSize() > maxFileSize
                || !allowedExtensions.contains(extension)
                || contentType == null
                || !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ValidationException(ErrorCode.FILE_INVALID);
        }
        String storedName = UUID.randomUUID() + "." + extension;
        chatFileStorage.store(file, storedName, extension);
        chatService.registerAttachment(
                conversationId,
                principal.getUserId(),
                storedName,
                originalName,
                contentType,
                file.getSize());
        return ChatFileUploadResponse.builder().url(FILE_URL_PREFIX + storedName).build();
    }

    public ChatFileDownloadDto download(AskPrincipal principal, String storedName) {
        ChatAttachmentDto attachment = chatService.findAttachmentByStoredName(storedName);
        if (attachment == null) {
            throw new NotFoundException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        requireParticipant(principal, attachment.getConversationId());
        Resource resource = chatFileStorage.resolve(storedName);
        if (resource == null) {
            throw new NotFoundException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
        return ChatFileDownloadDto.builder()
                .resource(resource)
                .contentType(attachment.getContentType())
                .originalName(attachment.getOriginalName())
                .build();
    }

    private void requireParticipant(AskPrincipal principal, UUID conversationId) {
        ChatConversationDto conversation = chatService.getConversation(conversationId);
        UUID userId = principal.getUserId();
        if (userId.equals(conversation.getCustomerId())) {
            return;
        }
        if (conversation.getBusinessId() != null
                && businessMemberService.findByBusinessAndUser(conversation.getBusinessId(), userId) != null) {
            return;
        }
        PlatformMembershipDto platformMembership =
                platformMembershipService.findActiveByUser(userId);
        if (platformMembership != null
                && "MANAGED_IMPORT".equals(conversation.getConversationType())
                && conversation.getBusinessId() != null
                && managedImportService.hasActiveGrant(conversation.getBusinessId(), userId)
                && hasManagedImportPermission(platformMembership)) {
            return;
        }
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasManagedImportPermission(PlatformMembershipDto membership) {
        return membership.getPermissions().contains(PlatformPermission.MANAGE_MANAGED_IMPORTS)
                || membership.getPermissions().contains(PlatformPermission.MANAGE_SUPPORT_CHATS);
    }

    private Set<String> splitConfig(String value) {
        return Arrays.stream(value.split(","))
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
