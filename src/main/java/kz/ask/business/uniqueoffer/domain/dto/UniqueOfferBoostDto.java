package kz.ask.business.uniqueoffer.domain.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UniqueOfferBoostDto {
    private UUID aggregateId;
    private List<UUID> branchIds;
    private String label;
}
