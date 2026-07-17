package kz.ask.business.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessMemberBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessMemberBranchServiceImpl implements BusinessMemberBranchService {

    private final BusinessMemberBranchRepository businessMemberBranchRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final BusinessBranchRepository businessBranchRepository;

    @Override
    @Transactional
    public void assign(UUID businessMembershipId, UUID branchId) {
        if (businessMemberBranchRepository.existsByBusinessMembershipIdAndBranchId(
                businessMembershipId, branchId)) {
            return;
        }
        BusinessMemberBranch assignment = new BusinessMemberBranch();
        assignment.setBusinessMembership(businessMemberRepository.getReferenceById(businessMembershipId));
        assignment.setBranch(businessBranchRepository.getReferenceById(branchId));
        businessMemberBranchRepository.save(assignment);
    }

    @Override
    public List<UUID> findBranchIds(UUID businessMembershipId) {
        return businessMemberBranchRepository.findByBusinessMembershipId(businessMembershipId).stream()
                .map(assignment -> assignment.getBranch().getId())
                .toList();
    }
}
