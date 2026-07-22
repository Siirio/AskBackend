package kz.ask.search.basic.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "search_query_alias")
public class SearchQueryAlias extends BaseUuidV7Entity {

    @Column(name = "alias_value", nullable = false)
    private String aliasValue;

    @Column(name = "target_query", nullable = false)
    private String targetQuery;

}
