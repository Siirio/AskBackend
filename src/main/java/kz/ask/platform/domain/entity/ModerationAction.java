package kz.ask.platform.domain.entity;

import jakarta.persistence.*;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import kz.ask.platform.domain.enums.ModerationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class ModerationAction extends BaseUuidV7Entity { //this needs to be onedirectional to every entity. Any entity can be moderated and taken action of if needed.

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus;

    @JoinColumn
    private AppUser madeBy;

    private <> beingMadeTo;

    private String reason;
}