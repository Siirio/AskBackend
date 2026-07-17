package kz.ask.platform.infrastructure.mapper;

import java.util.LinkedHashSet;
import java.util.List;
import kz.ask.platform.domain.dto.PlatformMembershipDto;
import kz.ask.platform.domain.entity.PlatformMembership;
import org.springframework.stereotype.Component;

@Component
public class PlatformMembershipMapper {

    public PlatformMembershipDto toDto(PlatformMembership entity) {
        return PlatformMembershipDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .email(entity.getUser().getEmail())
                .displayName(entity.getUser().getDisplayName())
                .role(entity.getRole())
                .status(entity.getStatus())
                .permissions(new LinkedHashSet<>(entity.getPermissions()))
                .build();
    }

    public List<PlatformMembershipDto> toDtoList(List<PlatformMembership> entities) {
        return entities.stream().map(this::toDto).toList();
    }
}
