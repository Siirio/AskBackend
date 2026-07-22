package kz.ask.business.infrastructure.mapper;

import java.util.LinkedHashSet;
import kz.ask.business.domain.dto.BusinessInvitationDto;
import kz.ask.business.domain.entity.BusinessInvitation;
import org.springframework.stereotype.Component;

@Component
public class BusinessInvitationMapper {

    public BusinessInvitationDto toDto(BusinessInvitation entity) {
        return BusinessInvitationDto.builder()
                .id(entity.getId())
                .businessId(entity.getBusiness().getId())
                .businessName(entity.getBusiness().getName())
                .invitedEmail(entity.getInvitedEmail())
                .invitedRole(entity.getInvitedRole().name())
                .invitedByUserId(entity.getInvitedBy().getId())
                .invitedByDisplayName(entity.getInvitedBy().getDisplayName())
                .status(entity.getStatus().name())
                .expiresAt(entity.getExpiresAt())
                .branchIds(new LinkedHashSet<>(entity.getBranchIds()))
                .build();
    }
}
