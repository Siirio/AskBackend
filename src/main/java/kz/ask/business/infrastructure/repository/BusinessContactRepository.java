package kz.ask.business.infrastructure.repository;

import java.util.UUID;
import kz.ask.business.domain.entity.BusinessContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessContactRepository extends JpaRepository<BusinessContact, UUID> {
}
