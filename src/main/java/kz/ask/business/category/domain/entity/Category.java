package kz.ask.business.category.domain.entity;

import kz.ask.business.category.domain.enums.CategorySource;
import kz.ask.business.category.domain.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;

@Entity
@Getter
@Setter
@Table(name = "category")
public class Category extends BaseUuidV7Entity {

    @Column(nullable = false)
    private String name;

    private String slug;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategorySource source;
}
