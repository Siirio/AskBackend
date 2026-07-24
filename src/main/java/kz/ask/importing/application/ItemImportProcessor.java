package kz.ask.importing.application;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import kz.ask.business.branch.domain.BusinessBranchService;
import kz.ask.business.branch.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.category.domain.CategoryService;
import kz.ask.business.category.domain.entity.Category;
import kz.ask.business.category.domain.enums.CategoryType;
import kz.ask.business.core.domain.BusinessService;
import kz.ask.business.core.domain.enums.BusinessScope;
import kz.ask.business.core.infrastructure.repository.BusinessRepository;
import kz.ask.business.member.domain.BranchMemberService;
import kz.ask.identity.infrastructure.security.AskPrincipal;
import kz.ask.importing.api.dto.ItemImportApproveResponse;
import kz.ask.importing.api.dto.ItemImportCancelResponse;
import kz.ask.importing.api.dto.ItemImportColumnInfo;
import kz.ask.importing.api.dto.ItemImportMappingEntry;
import kz.ask.importing.api.dto.ItemImportMappingRequest;
import kz.ask.importing.api.dto.ItemImportPreviewResponse;
import kz.ask.importing.api.dto.ItemImportRowResponse;
import kz.ask.importing.api.dto.ItemImportTargetField;
import kz.ask.importing.api.dto.ItemImportUploadResponse;
import kz.ask.managedimport.domain.ManagedImportService;
import kz.ask.offer.item.domain.entity.Item;
import kz.ask.offer.item.domain.enums.ProductModerationStatus;
import kz.ask.offer.item.infrastructure.repository.ProductRepository;
import kz.ask.offer.service.api.dto.BusinessServiceCreateRequest;
import kz.ask.offer.service.application.ServiceOfferingDto;
import kz.ask.offer.service.domain.ServiceService;
import kz.ask.offer.service.domain.enums.ServiceMode;
import kz.ask.search.basic.domain.SearchOutboxService;
import kz.ask.search.basic.domain.enums.SearchAggregateType;
import kz.ask.search.basic.domain.enums.SearchEventType;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.ForbiddenException;
import kz.ask.shared.error.InternalServerException;
import kz.ask.shared.error.NotFoundException;
import kz.ask.shared.error.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ItemImportProcessor {

    private static final String MAPPING_REQUIRED = "MAPPING_REQUIRED";
    private static final String PREVIEW_READY = "PREVIEW_READY";
    private static final String IMPORTING = "IMPORTING";
    private static final String IMPORTED = "IMPORTED";
    private static final String CANCELLED = "CANCELLED";

    private final Map<UUID, ItemImportSession> sessions = new ConcurrentHashMap<>();
    private final ItemImportExcelParser excelParser;
    private final ItemImportAutoMapper autoMapper;
    private final BusinessService businessService;
    private final BusinessBranchService businessBranchService;
    private final BranchMemberService branchMemberService;
    private final ManagedImportService managedImportService;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ServiceService serviceService;
    private final SearchOutboxService searchOutboxService;

    @Value("${business.item-import.default-category-name:Общее}")
    private String defaultCategoryName;

    public ItemImportUploadResponse upload(AskPrincipal principal, UUID businessId, UUID branchId,
                                           CategoryType type, MultipartFile file) {
        validateType(type);
        requireAccess(principal.getUserId(), businessId, branchId, type);
        validateFile(file);
        try {
            ItemImportParsedWorkbook workbook = excelParser.parse(file.getInputStream());
            if (workbook.getColumns().isEmpty()) {
                throw new ValidationException(ErrorCode.IMPORT_EMPTY_FILE);
            }
            List<ItemImportColumnInfo> columns = workbook.getColumns().stream()
                    .map(autoMapper::suggest)
                    .toList();
            List<ItemImportMappingEntry> mappings = columns.stream()
                    .map(column -> ItemImportMappingEntry.builder()
                            .sourceColumn(column.getSourceColumn())
                            .targetField(column.getSuggestedTargetField())
                            .build())
                    .toList();
            UUID importId = UUID.randomUUID();
            List<ItemImportRow> rows = new ArrayList<>();
            for (int index = 0; index < workbook.getRows().size(); index++) {
                rows.add(ItemImportRow.builder()
                        .id(UUID.randomUUID())
                        .rowNumber(index + 1)
                        .source(workbook.getRows().get(index))
                        .normalized(Map.of())
                        .status("PENDING")
                        .errors(List.of())
                        .warnings(List.of())
                        .build());
            }
            sessions.put(importId, ItemImportSession.builder()
                    .id(importId)
                    .businessId(businessId)
                    .branchId(branchId)
                    .createdByUserId(principal.getUserId())
                    .type(type)
                    .originalFileName(file.getOriginalFilename())
                    .status(MAPPING_REQUIRED)
                    .columns(workbook.getColumns())
                    .mappings(mappings)
                    .rows(rows)
                    .build());
            return ItemImportUploadResponse.builder()
                    .importId(importId)
                    .originalFileName(file.getOriginalFilename())
                    .status(MAPPING_REQUIRED)
                    .totalRows(rows.size())
                    .columns(columns)
                    .sampleRows(workbook.getRows().stream().limit(3).toList())
                    .build();
        } catch (IOException exception) {
            throw new InternalServerException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public ItemImportPreviewResponse map(AskPrincipal principal, UUID importId,
                                         ItemImportMappingRequest request) {
        ItemImportSession session = requireSession(principal, importId);
        if (!MAPPING_REQUIRED.equals(session.getStatus())) {
            throw new ValidationException(ErrorCode.IMPORT_INVALID_STATUS);
        }
        validateMappings(session, request.getMappings());
        session.setMappings(List.copyOf(request.getMappings()));
        session.getRows().forEach(row -> normalize(row, session.getMappings()));
        session.setStatus(PREVIEW_READY);
        return preview(session);
    }

    public ItemImportPreviewResponse preview(AskPrincipal principal, UUID importId) {
        return preview(requireSession(principal, importId));
    }

    @Transactional
    public ItemImportApproveResponse approve(AskPrincipal principal, UUID importId) {
        ItemImportSession session = requireSession(principal, importId);
        UUID businessId = session.getBusinessId();
        requireAccess(principal.getUserId(), businessId, session.getBranchId(), session.getType());
        synchronized (session) {
            if (!PREVIEW_READY.equals(session.getStatus())) {
                throw new ValidationException(ErrorCode.IMPORT_INVALID_STATUS);
            }
            session.setStatus(IMPORTING);
        }
        TransactionSynchronizationManager.registerSynchronization(
                new ItemImportTransactionSynchronization(
                        () -> sessions.remove(importId),
                        () -> session.setStatus(PREVIEW_READY)));
        int created = 0;
        int skipped = 0;
        for (ItemImportRow row : session.getRows()) {
            if ("INVALID".equals(row.getStatus())) {
                skipped++;
                continue;
            }
            if (session.getType() == CategoryType.ITEM) {
                createItem(session, row);
            } else {
                createService(session, row);
            }
            created++;
        }
        session.setStatus(IMPORTED);
        return ItemImportApproveResponse.builder()
                .importId(importId)
                .status(IMPORTED)
                .productsCreated(created)
                .offersCreated(created)
                .rowsSkipped(skipped)
                .build();
    }

    public ItemImportCancelResponse cancel(AskPrincipal principal, UUID importId) {
        ItemImportSession session = requireSession(principal, importId);
        synchronized (session) {
            if (IMPORTING.equals(session.getStatus()) || IMPORTED.equals(session.getStatus())) {
                throw new ValidationException(ErrorCode.IMPORT_INVALID_STATUS);
            }
            session.setStatus(CANCELLED);
            sessions.remove(importId);
        }
        return ItemImportCancelResponse.builder().importId(importId).status(CANCELLED).build();
    }

    private void createItem(ItemImportSession session, ItemImportRow row) {
        Map<String, String> data = row.getNormalized();
        Category category = categoryService.resolveOrCreate(
                valueOrDefault(data.get(ItemImportTargetField.CATEGORY_LABEL.name()), defaultCategoryName),
                CategoryType.ITEM);
        Item item = new Item();
        item.setBusiness(businessRepository.getReferenceById(session.getBusinessId()));
        item.setBranch(session.getBranchId() == null
                ? null : businessBranchRepository.getReferenceById(session.getBranchId()));
        item.setCategory(category);
        item.setName(data.get(ItemImportTargetField.NAME.name()));
        item.setDescription(emptyToNull(data.get(ItemImportTargetField.DESCRIPTION.name())));
        item.setTags(csv(data.get(ItemImportTargetField.TAGS.name())));
        item.setPrice(price(data.get(ItemImportTargetField.PRICE.name())));
        item.setIsActive(Boolean.TRUE);
        item.setModerationStatus(ProductModerationStatus.PENDING);
        item.setAttributes(attributes(data));
        Item saved = productRepository.save(item);
        searchOutboxService.publish(SearchAggregateType.PRODUCT_OFFER, saved.getId(),
                SearchEventType.UPSERT, Instant.now().toEpochMilli());
    }

    private void createService(ItemImportSession session, ItemImportRow row) {
        Map<String, String> data = row.getNormalized();
        ServiceOfferingDto saved = serviceService.createService(
                session.getBusinessId(), BusinessServiceCreateRequest.builder()
                .branchId(session.getBranchId())
                .categoryName(valueOrDefault(
                        data.get(ItemImportTargetField.CATEGORY_LABEL.name()), defaultCategoryName))
                .name(data.get(ItemImportTargetField.NAME.name()))
                .description(emptyToNull(data.get(ItemImportTargetField.DESCRIPTION.name())))
                .serviceMode(ServiceMode.ON_DEMAND)
                .basePrice(price(data.get(ItemImportTargetField.PRICE.name())))
                .isActive(Boolean.TRUE)
                .attributes(attributes(data))
                .build());
        searchOutboxService.publish(SearchAggregateType.SERVICE_BRANCH_OFFER, saved.getId(),
                SearchEventType.UPSERT, Instant.now().toEpochMilli());
    }

    private void normalize(ItemImportRow row, List<ItemImportMappingEntry> mappings) {
        Map<String, String> normalized = new LinkedHashMap<>();
        Map<String, String> attributes = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        for (ItemImportMappingEntry mapping : mappings) {
            String value = row.getSource().getOrDefault(mapping.getSourceColumn(), "").trim();
            if (value.isEmpty() || mapping.getTargetField() == ItemImportTargetField.IGNORE) continue;
            if (mapping.getTargetField() == ItemImportTargetField.CHARACTERISTIC) {
                String key = valueOrDefault(mapping.getCharacteristicName(), mapping.getSourceColumn());
                attributes.put(key, value);
            } else if (mapping.getTargetField() == ItemImportTargetField.APPEND_TO_DESCRIPTION) {
                normalized.merge(ItemImportTargetField.DESCRIPTION.name(), value, (left, right) -> left + "\n" + right);
            } else if (mapping.getTargetField() == ItemImportTargetField.SKU) {
                attributes.put(ItemImportTargetField.SKU.name(), value);
            } else {
                normalized.put(mapping.getTargetField().name(), value);
            }
        }
        attributes.forEach((key, value) -> normalized.put("ATTRIBUTE:" + key, value));
        List<String> errors = new ArrayList<>();
        if (normalized.getOrDefault(ItemImportTargetField.NAME.name(), "").isBlank()) {
            errors.add(ErrorCode.IMPORT_NAME_REQUIRED.format());
        }
        String rawPrice = normalized.get(ItemImportTargetField.PRICE.name());
        if (rawPrice != null && price(rawPrice) == null) {
            warnings.add(rawPrice);
            normalized.remove(ItemImportTargetField.PRICE.name());
        }
        row.setNormalized(normalized);
        row.setErrors(errors);
        row.setWarnings(warnings);
        row.setStatus(!errors.isEmpty() ? "INVALID" : !warnings.isEmpty() ? "WARNING" : "VALID");
    }

    private ItemImportPreviewResponse preview(ItemImportSession session) {
        List<ItemImportRowResponse> rows = session.getRows().stream()
                .map(row -> ItemImportRowResponse.builder()
                        .rowId(row.getId())
                        .rowNumber(row.getRowNumber())
                        .status(row.getStatus())
                        .normalizedData(row.getNormalized())
                        .errors(row.getErrors())
                        .warnings(row.getWarnings())
                        .build())
                .toList();
        int valid = (int) rows.stream().filter(row -> "VALID".equals(row.getStatus())).count();
        int invalid = (int) rows.stream().filter(row -> "INVALID".equals(row.getStatus())).count();
        int warning = (int) rows.stream().filter(row -> "WARNING".equals(row.getStatus())).count();
        return ItemImportPreviewResponse.builder()
                .importId(session.getId())
                .status(session.getStatus())
                .totalRows(rows.size())
                .validRows(valid)
                .invalidRows(invalid)
                .warningRows(warning)
                .mappings(session.getMappings())
                .rows(rows)
                .build();
    }

    private ItemImportSession requireSession(AskPrincipal principal, UUID importId) {
        ItemImportSession session = sessions.get(importId);
        if (session == null) {
            throw new NotFoundException(ErrorCode.IMPORT_NOT_FOUND);
        }
        if (!principal.getUserId().equals(session.getCreatedByUserId())) {
            throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
        }
        return session;
    }

    private void requireAccess(UUID userId, UUID businessId, UUID branchId, CategoryType type) {
        if (branchId != null && businessBranchService.findByBusinessAndId(businessId, branchId) == null) {
            throw new NotFoundException(ErrorCode.BRANCH_NOT_FOUND);
        }
        if (businessService.isManagerOrAboveOfBusiness(businessId, userId)) return;
        if (branchId != null && branchMemberService.isStaffOfBranch(branchId, userId)) return;
        BusinessScope scope = managedImportService.activeScope(businessId, userId);
        if (scope == BusinessScope.BOTH
                || type == CategoryType.ITEM && scope == BusinessScope.ITEM
                || type == CategoryType.SERVICE && scope == BusinessScope.SERVICE) return;
        throw new ForbiddenException(ErrorCode.ACCESS_DENIED);
    }

    private void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new ValidationException(ErrorCode.IMPORT_NOT_XLSX);
        }
        if (file.isEmpty()) throw new ValidationException(ErrorCode.IMPORT_EMPTY_FILE);
    }

    private void validateType(CategoryType type) {
        if (type != CategoryType.ITEM && type != CategoryType.SERVICE) {
            throw new ValidationException(ErrorCode.BUSINESS_SCOPE_REQUIRED);
        }
    }

    private void validateMappings(ItemImportSession session, List<ItemImportMappingEntry> mappings) {
        for (ItemImportMappingEntry mapping : mappings) {
            if (!session.getColumns().contains(mapping.getSourceColumn())) {
                throw new ValidationException(ErrorCode.IMPORT_COLUMN_NOT_FOUND, mapping.getSourceColumn());
            }
        }
        if (mappings.stream().noneMatch(mapping -> mapping.getTargetField() == ItemImportTargetField.NAME)) {
            throw new ValidationException(ErrorCode.IMPORT_NAME_REQUIRED);
        }
    }

    private Map<String, Object> attributes(Map<String, String> normalized) {
        Map<String, Object> result = new LinkedHashMap<>();
        normalized.forEach((key, value) -> {
            if (key.startsWith("ATTRIBUTE:")) result.put(key.substring("ATTRIBUTE:".length()), value);
        });
        return result;
    }

    private List<String> csv(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split(",")).stream().map(String::trim).filter(item -> !item.isEmpty()).toList();
    }

    private BigDecimal price(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value.replace(" ", "").replace(',', '.'));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
