package kz.ask.shared.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class EditableBaseEntity extends BaseUuidV7Entity {

    @Column(name = "edited_by")
    private UUID editedBy;
}
