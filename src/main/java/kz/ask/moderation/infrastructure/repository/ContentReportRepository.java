package kz.ask.moderation.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.moderation.domain.entity.ContentReport;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {

    @EntityGraph(attributePaths = {"reporter", "resolvedBy"})
    List<ContentReport> findByStatusOrderByCreatedAtAsc(ContentReportStatus status);

    Long countByStatus(ContentReportStatus status);
}
