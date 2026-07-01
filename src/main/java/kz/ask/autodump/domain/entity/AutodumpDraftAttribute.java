package kz.ask.autodump.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kz.ask.autodump.domain.enums.DraftAttributeSource;
import kz.ask.shared.domain.entity.BaseUuidV7Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "autodump_draft_attribute")
public class AutodumpDraftAttribute extends BaseUuidV7Entity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_item_id", nullable = false)
    private AutodumpDraftItem draftItem;

    @Column(nullable = false)
    private String attributeKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String attributeValue;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DraftAttributeSource source;
}
