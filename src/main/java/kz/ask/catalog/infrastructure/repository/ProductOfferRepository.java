package kz.ask.catalog.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kz.ask.catalog.domain.entity.ProductOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOfferRepository extends JpaRepository<ProductOffer, UUID> {

    List<ProductOffer> findByProductId(UUID productId);

    @Query("select po from ProductOffer po where po.product.business.id = :businessId")
    List<ProductOffer> findByBusinessId(@Param("businessId") UUID businessId);

    @Query("""
        select count(distinct po.product.id) from ProductOffer po
        where po.product.business.id = :businessId
          and po.product.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
          and po.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
          and po.enabled = true
        """)
    long countActiveProductsByBusinessId(@Param("businessId") UUID businessId);

    @Query("""
        select po from ProductOffer po
        join fetch po.product p
        join fetch p.category c
        join fetch po.branch b
        where p.id = :productId and po.branch.id = :branchId
        """)
    Optional<ProductOffer> findByProductIdAndBranchId(@Param("productId") UUID productId, @Param("branchId") UUID branchId);

    @Query("""
        select distinct po from ProductOffer po
        join fetch po.product p
        join fetch p.business
        left join fetch p.category
        left join fetch p.tags
        join fetch po.branch b
        left join fetch b.city
        where po.id = :offerId
        """)
    Optional<ProductOffer> findProjectionSourceById(@Param("offerId") UUID offerId);

    @Query("""
        select po from ProductOffer po
        join fetch po.product p
        join fetch p.business
        join fetch po.branch
        where (:afterId is null or po.id > :afterId)
        order by po.id
        """)
    List<ProductOffer> findReconciliationBatch(@Param("afterId") UUID afterId, Pageable pageable);

    @Query(value = """
        select distinct po from ProductOffer po
        join fetch po.product p
        left join fetch p.category c
        join fetch po.branch b
        left join p.tags t
        where po.branch.id = :branchId
          and po.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
          and (:categoryId is null or p.category.id = :categoryId)
          and (:enabled is null or po.enabled = :enabled)
          and (:term is null or lower(p.name) like :term or lower(p.sku) like :term or lower(t) like :term)
        """,
        countQuery = """
        select count(distinct po) from ProductOffer po
        join po.product p
        left join p.tags t
        where po.branch.id = :branchId
          and po.status = kz.ask.shared.domain.enums.RecordStatus.ACTIVE
          and (:categoryId is null or p.category.id = :categoryId)
          and (:enabled is null or po.enabled = :enabled)
          and (:term is null or lower(p.name) like :term or lower(p.sku) like :term or lower(t) like :term)
        """)
    Page<ProductOffer> search(@Param("branchId") UUID branchId,
                               @Param("categoryId") UUID categoryId,
                               @Param("enabled") Boolean enabled,
                               @Param("term") String term,
                               Pageable pageable);
}
