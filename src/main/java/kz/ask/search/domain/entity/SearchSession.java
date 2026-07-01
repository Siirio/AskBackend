package kz.ask.search.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import kz.ask.business.domain.entity.Category;
import kz.ask.business.domain.entity.City;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.search.domain.enums.SearchScope;
import kz.ask.search.domain.enums.SearchSessionStatus;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "search_session")
public class SearchSession extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String rawQuery;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchScope scope;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchSessionStatus status;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant lastActiveAt;

    private Instant snapshotExpiresAt;
}
