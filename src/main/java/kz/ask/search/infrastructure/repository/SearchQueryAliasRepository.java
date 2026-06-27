package kz.ask.search.infrastructure.repository;

import java.util.List;
import java.util.UUID;
import kz.ask.search.domain.entity.SearchQueryAlias;
import kz.ask.shared.domain.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchQueryAliasRepository extends JpaRepository<SearchQueryAlias, UUID> {

    List<SearchQueryAlias> findByAliasValueAndStatus(String aliasValue, RecordStatus status);
}
