package kz.ask.service.infrastructure.repository;


import kz.ask.service.domain.entity.ServiceBranchOffer;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;


public class ServiceBranchOfferSpecification {

    public static Specification<ServiceBranchOffer> hasBranch(UUID branchId){
        return  ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("branch").get("id"), branchId));
    }

    public static Specification<ServiceBranchOffer> hasCategory(UUID categoryId){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("serviceOffering").get("category").get("id"), categoryId);
    }

    public static Specification<ServiceBranchOffer> isActive(Boolean active){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<ServiceBranchOffer> nameContains(String queryName){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("serviceOffering").get("name"), "%" + queryName + "%");
    }

}
