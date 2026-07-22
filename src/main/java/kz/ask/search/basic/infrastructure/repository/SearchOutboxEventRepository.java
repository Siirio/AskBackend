package kz.ask.search.basic.infrastructure.repository;

import java.time.Instant;
import java.util.UUID;
import kz.ask.search.basic.domain.entity.SearchOutboxEvent;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchOutboxEventRepository extends JpaRepository<SearchOutboxEvent, UUID> {

    @Modifying
    @Query(value = """
        insert into search_outbox_event (
            id, aggregate_type, aggregate_id, event_type, aggregate_version,
            payload_version, available_at, attempt_count, status, created_at, updated_at
        ) values (
            :id, :aggregateType, :aggregateId, :eventType, :aggregateVersion,
            :payloadVersion, :availableAt, 0, 'PENDING', :createdAt, :createdAt
        )
        on conflict (aggregate_type, aggregate_id, aggregate_version, event_type) do nothing
        """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("aggregateType") String aggregateType,
                       @Param("aggregateId") UUID aggregateId,
                       @Param("eventType") String eventType,
                       @Param("aggregateVersion") Long aggregateVersion,
                       @Param("payloadVersion") Integer payloadVersion,
                       @Param("availableAt") Instant availableAt,
                       @Param("createdAt") Instant createdAt);

    @Modifying
    @Query(value = """
        insert into search_outbox_event (
            id, aggregate_type, aggregate_id, event_type, aggregate_version,
            payload_version, available_at, attempt_count, status, created_at, updated_at
        ) values (
            :id, :aggregateType, :aggregateId, :eventType, :aggregateVersion,
            :payloadVersion, :availableAt, 0, 'PENDING', :createdAt, :createdAt
        )
        on conflict (aggregate_type, aggregate_id, aggregate_version, event_type)
        do update set status = 'PENDING', available_at = excluded.available_at,
            processing_started_at = null, processed_at = null, worker_id = null,
            attempt_count = 0, last_error = null, updated_at = excluded.updated_at
        where search_outbox_event.status <> 'PROCESSING'
        """, nativeQuery = true)
    int requeue(@Param("id") UUID id,
                @Param("aggregateType") String aggregateType,
                @Param("aggregateId") UUID aggregateId,
                @Param("eventType") String eventType,
                @Param("aggregateVersion") Long aggregateVersion,
                @Param("payloadVersion") Integer payloadVersion,
                @Param("availableAt") Instant availableAt,
                @Param("createdAt") Instant createdAt);

    @Modifying
    @Query(value = """
        update search_outbox_event older
        set status = 'COMPLETED', processed_at = :now, updated_at = :now,
            last_error = 'Superseded by a newer aggregate version'
        where older.status in ('PENDING', 'RETRY')
          and exists (
              select 1 from search_outbox_event newer
              where newer.aggregate_type = older.aggregate_type
                and newer.aggregate_id = older.aggregate_id
                and newer.aggregate_version > older.aggregate_version
          )
        """, nativeQuery = true)
    int completeSuperseded(@Param("now") Instant now);

    @Query(value = """
        select * from search_outbox_event
        where status in ('PENDING', 'RETRY')
          and available_at <= :now
        order by available_at, created_at
        limit :batchSize
        for update skip locked
        """, nativeQuery = true)
    List<SearchOutboxEvent> lockClaimable(@Param("now") Instant now,
                                          @Param("batchSize") Integer batchSize);

    @Modifying
    @Query(value = """
        update search_outbox_event
        set status = 'RETRY', worker_id = null, processing_started_at = null,
            available_at = :availableAt, last_error = :lastError, updated_at = :now
        where id = :id and status = 'PROCESSING' and worker_id = :workerId
        """, nativeQuery = true)
    int markRetry(@Param("id") UUID id,
                  @Param("workerId") String workerId,
                  @Param("availableAt") Instant availableAt,
                  @Param("lastError") String lastError,
                  @Param("now") Instant now);

    @Modifying
    @Query(value = """
        update search_outbox_event
        set status = :status, worker_id = null, processing_started_at = null,
            processed_at = :processedAt, last_error = :lastError, updated_at = :processedAt
        where id = :id and status = 'PROCESSING' and worker_id = :workerId
        """, nativeQuery = true)
    int finish(@Param("id") UUID id,
               @Param("workerId") String workerId,
               @Param("status") String status,
               @Param("processedAt") Instant processedAt,
               @Param("lastError") String lastError);

    @Modifying
    @Query(value = """
        update search_outbox_event
        set status = 'RETRY', worker_id = null, processing_started_at = null,
            available_at = :now, last_error = 'Recovered abandoned processing lease', updated_at = :now
        where status = 'PROCESSING' and processing_started_at < :staleBefore
        """, nativeQuery = true)
    int recoverAbandoned(@Param("staleBefore") Instant staleBefore, @Param("now") Instant now);

    @Modifying
    @Query(value = """
        delete from search_outbox_event
        where status = 'COMPLETED' and processed_at < :cutoff
        """, nativeQuery = true)
    int deleteCompletedBefore(@Param("cutoff") Instant cutoff);
}
