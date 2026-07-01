package kz.ask.business.domain.dto;

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
    private BusinessContactDto contact;
}
