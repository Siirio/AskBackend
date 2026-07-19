package kz.ask.managedimport.domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.CatalogScope;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.managedimport.domain.dto.ManagedImportDto;

public interface ManagedImportService {

    ManagedImportDto create(
            UUID businessId,
            UUID requestedByUserId,
            CatalogScope catalogScope,
            Set<CatalogSourceType> sourceTypes,
            PreferredContactChannel preferredContactChannel,
            String preferredContactValue,
            String sourceLinks,
            String sourceNotes);

    List<ManagedImportDto> listOpen();

    List<ManagedImportDto> listForBusiness(UUID businessId);

    ManagedImportDto activate(UUID requestId, UUID platformUserId);

    Boolean hasActiveGrant(UUID businessId, UUID platformUserId);

    CatalogScope activeScope(UUID businessId, UUID platformUserId);

    void expireDue(Instant now);
}
