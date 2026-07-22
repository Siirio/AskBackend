package kz.ask.business.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.domain.entity.BusinessMember;
import kz.ask.business.domain.enums.BusinessMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, UUID> {

    List<BusinessMember> findByUserIdAndRole(UUID userId, BusinessMemberRole role);

    List<BusinessMember> findByUserId(UUID userId);

    List<BusinessMember> findByBusinessIdAndUserId(UUID businessId, UUID userId);

    List<BusinessMember> findByBusinessId(UUID businessId);
}
