package kz.ask.audit.infrastructure.repository;

import java.util.UUID;
import kz.ask.audit.domain.entity.SignificantEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignificantEventRepository extends JpaRepository<SignificantEvent, UUID> {
}
