package kz.ask.search.basic.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.search.basic.domain.entity.SearchQueryAlias;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchQueryAliasRepository extends JpaRepository<SearchQueryAlias, UUID> {

    List<SearchQueryAlias> findByAliasValue(String aliasValue);
}
