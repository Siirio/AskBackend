package kz.ask.service.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import kz.ask.service.domain.entity.ServiceBranchOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceBranchOfferRepository extends JpaRepository<ServiceBranchOffer, UUID> {

    @Query("""
        select sbo from ServiceBranchOffer sbo
        join fetch sbo.serviceOffering so
        join fetch so.category c
        join fetch sbo.branch b
        where so.id = :serviceOfferingId and sbo.branch.id = :branchId
        """)
    Optional<ServiceBranchOffer> findByServiceOfferingIdAndBranchId(@Param("serviceOfferingId") UUID serviceOfferingId,
                                                                     @Param("branchId") UUID branchId);

    @Query(value = """
        select distinct sbo from ServiceBranchOffer sbo
        join fetch sbo.serviceOffering so
        join fetch so.category c
        join fetch sbo.branch b
        where sbo.branch.id = :branchId
          and sbo.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
          and (:categoryId is null or so.category.id = :categoryId)
          and (:active is null or sbo.active = :active)
          and (:term is null or lower(so.name) like :term or lower(so.description) like :term)
        """,
        countQuery = """
        select count(distinct sbo) from ServiceBranchOffer sbo
        join sbo.serviceOffering so
        where sbo.branch.id = :branchId
          and sbo.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
          and (:categoryId is null or so.category.id = :categoryId)
          and (:active is null or sbo.active = :active)
          and (:term is null or lower(so.name) like :term or lower(so.description) like :term)
        """)
    Page<ServiceBranchOffer> search(@Param("branchId") UUID branchId,
                                     @Param("categoryId") UUID categoryId,
                                     @Param("active") Boolean active,
                                     @Param("term") String term,
                                     Pageable pageable);
}
