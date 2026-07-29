package kz.ask.business.core.domain.dto;

import kz.ask.business.branch.domain.dto.BusinessBranchDto;
import kz.ask.business.member.domain.dto.BusinessMemberDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRegistrationResult {
    private BusinessDto business;
    private BusinessBranchDto branch;
    private BusinessMemberDto member;
}
