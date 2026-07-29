package kz.ask.search.basic.infrastructure.repository;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchAggregateLockRepository {

    private final EntityManager entityManager;

    public void lock(UUID aggregateId) {
        entityManager.createNativeQuery(
                        "select pg_advisory_xact_lock(hashtextextended(cast(:aggregateId as text), 0))")
                .setParameter("aggregateId", aggregateId)
                .getSingleResult();
    }
}
