package kz.ask.managedimport.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import kz.ask.business.domain.enums.CatalogSourceType;
import kz.ask.business.domain.enums.PreferredContactChannel;
import kz.ask.managedimport.domain.dto.ManagedImportDto;

public interface ManagedImportService {

    ManagedImportDto create(
            UUID businessId,
            UUID requestedByUserId,
            Set<CatalogSourceType> sourceTypes,
            PreferredContactChannel preferredContactChannel,
            String preferredContactValue,
            String sourceLinks,
            String sourceNotes);

    List<ManagedImportDto> listOpen();

    List<ManagedImportDto> listForBusiness(UUID businessId);

    ManagedImportDto activate(UUID requestId, UUID platformUserId);

    ManagedImportDto complete(
            UUID requestId,
            UUID platformUserId,
            Integer productsPublishedCount);

    Boolean hasActiveGrant(UUID businessId);
}
