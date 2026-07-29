package kz.ask.business.member.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import kz.ask.business.member.domain.dto.BusinessMemberDto;

@Getter
@Builder
public class BusinessMemberListResponse {
    private List<BusinessMemberDto> members;
}
