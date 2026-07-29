package kz.ask.business.member.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.business.member.domain.entity.BusinessMember;
import kz.ask.identity.authorization.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, UUID> {

    List<BusinessMember> findByUserIdAndRole(UUID userId, Role role);

    List<BusinessMember> findByUserId(UUID userId);

    List<BusinessMember> findByBusinessIdAndUserId(UUID businessId, UUID userId);

    List<BusinessMember> findByBusinessId(UUID businessId);
}
