package kz.ask.managedimport.domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.core.domain.enums.ImportSourceType;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.domain.enums.PreferredContactChannel;
import kz.ask.managedimport.domain.dto.ManagedImportDto;

public interface ManagedImportService {

    ManagedImportDto create(
            UUID businessId,
            UUID requestedByUserId,
            BusinessScope businessScope,
            Set<ImportSourceType> selectedSourceTypes,
            PreferredContactChannel preferredContactChannel,
            String preferredContactValue,
            String sourceLinks,
            String sourceNotes);

    List<ManagedImportDto> listOpen();

    List<ManagedImportDto> listForBusiness(UUID businessId);

    ManagedImportDto activate(UUID requestId, UUID platformUserId);

    Boolean hasActiveGrant(UUID businessId, UUID platformUserId);

    BusinessScope activeScope(UUID businessId, UUID platformUserId);

    void expireDue(Instant now);
}
