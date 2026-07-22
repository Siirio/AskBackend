package kz.ask.shared.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "city")
public class City extends BaseUuidV7Entity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String countryCode;

}

