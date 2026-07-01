package kz.ask.autodump.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.dto.AiJobDto;
import kz.ask.autodump.domain.dto.ImportSessionDto;
import kz.ask.autodump.domain.dto.RawInputDto;
import kz.ask.autodump.domain.entity.AutodumpImportSession;
import kz.ask.autodump.domain.enums.AiJobStatus;
import kz.ask.autodump.domain.enums.ImportSessionStatus;
import kz.ask.autodump.domain.enums.SourceType;
import kz.ask.autodump.domain.enums.StorageKind;
import kz.ask.autodump.infrastructure.mapper.AutodumpMapper;
import kz.ask.autodump.infrastructure.repository.AutodumpAiJobRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpImportSessionRepository;
import kz.ask.autodump.infrastructure.repository.AutodumpRawInputRepository;
import kz.ask.business.domain.entity.Business;
import kz.ask.business.domain.entity.BusinessBranch;
import kz.ask.business.infrastructure.repository.BusinessBranchRepository;
import kz.ask.business.infrastructure.repository.BusinessRepository;
import kz.ask.identity.domain.entity.AppUser;
import kz.ask.identity.infrastructure.repository.AppUserRepository;
import kz.ask.shared.error.ErrorCode;
import kz.ask.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutodumpImportServiceImpl implements AutodumpImportService {

    private final AutodumpImportSessionRepository sessionRepository;
    private final AutodumpRawInputRepository rawInputRepository;
    private final AutodumpAiJobRepository aiJobRepository;
    private final BusinessRepository businessRepository;
    private final BusinessBranchRepository businessBranchRepository;
    private final AppUserRepository appUserRepository;
    private final AutodumpMapper mapper;

    @Override
    @Transactional
    public ImportSessionDto createSession(UUID businessId, UUID branchId, UUID createdBy,
                                           SourceType sourceType, String inputSummary) {
        Business business = businessRepository.getReferenceById(businessId);
        BusinessBranch branch = businessBranchRepository.getReferenceById(branchId);
        AppUser user = appUserRepository.getReferenceById(createdBy);
        AutodumpImportSession entity = mapper.toImportSessionEntity(business, branch, user,
                sourceType, ImportSessionStatus.CREATED);
        entity.setInputSummary(inputSummary);
        AutodumpImportSession saved = sessionRepository.save(entity);
        return mapper.toImportSessionDto(saved);
    }

    @Override
    public ImportSessionDto findById(UUID sessionId) {
        AutodumpImportSession entity = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AUTODUMP_SESSION_NOT_FOUND));
        return mapper.toImportSessionDto(entity);
    }

    @Override
    public ImportSessionDto findByIdAndBranch(UUID sessionId, UUID branchId) {
        AutodumpImportSession entity = sessionRepository.findByIdAndBranchId(sessionId, branchId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AUTODUMP_SESSION_NOT_FOUND));
        return mapper.toImportSessionDto(entity);
    }

    @Override
    public List<ImportSessionDto> findByBranch(UUID branchId) {
        return sessionRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(mapper::toImportSessionDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(UUID sessionId, ImportSessionStatus status) {
        AutodumpImportSession entity = sessionRepository.getReferenceById(sessionId);
        entity.setStatus(status);
        if (status == ImportSessionStatus.PUBLISHED || status == ImportSessionStatus.FAILED
                || status == ImportSessionStatus.CANCELLED) {
            entity.setCompletedAt(Instant.now());
        }
        sessionRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateCounts(UUID sessionId, Integer totalDrafts, Integer approved, Integer rejected, Integer errors) {
        AutodumpImportSession entity = sessionRepository.getReferenceById(sessionId);
        entity.setTotalDraftCount(totalDrafts);
        entity.setApprovedCount(approved);
        entity.setRejectedCount(rejected);
        entity.setErrorCount(errors);
        sessionRepository.save(entity);
    }

    @Override
    @Transactional
    public RawInputDto storeRawInput(UUID sessionId, String originalFileName, String contentType, String rawText) {
        AutodumpImportSession session = sessionRepository.getReferenceById(sessionId);
        byte[] bytes = rawText.getBytes(StandardCharsets.UTF_8);
        String sha256 = sha256Hex(bytes);
        long sizeBytes = bytes.length;
        var entity = mapper.toRawInputEntity(session, originalFileName, contentType,
                StorageKind.DATABASE_TEXT, rawText, sha256, sizeBytes);
        var saved = rawInputRepository.save(entity);
        return mapper.toRawInputDto(saved);
    }

    @Override
    @Transactional
    public AiJobDto createAiJob(UUID sessionId, UUID rawInputId, String provider, String model, String promptVersion) {
        AutodumpImportSession session = sessionRepository.getReferenceById(sessionId);
        var rawInput = rawInputRepository.getReferenceById(rawInputId);
        var entity = mapper.toAiJobEntity(session, rawInput, AiJobStatus.QUEUED, provider, model, promptVersion);
        var saved = aiJobRepository.save(entity);
        return mapper.toAiJobDto(saved);
    }

    @Override
    @Transactional
    public void updateAiJobStatus(UUID jobId, AiJobStatus status, String errorMessage) {
        var entity = aiJobRepository.getReferenceById(jobId);
        entity.setStatus(status);
        entity.setErrorMessage(errorMessage);
        if (status == AiJobStatus.RUNNING && entity.getStartedAt() == null) {
            entity.setStartedAt(Instant.now());
        }
        aiJobRepository.save(entity);
    }

    @Override
    @Transactional
    public void completeAiJob(UUID jobId, String rawResponseJson, Integer inputTokens, Integer outputTokens) {
        var entity = aiJobRepository.getReferenceById(jobId);
        entity.setStatus(AiJobStatus.SUCCEEDED);
        entity.setRawResponseJson(rawResponseJson);
        entity.setInputTokenEstimate(inputTokens);
        entity.setOutputTokenEstimate(outputTokens);
        entity.setFinishedAt(Instant.now());
        if (entity.getStartedAt() == null) {
            entity.setStartedAt(Instant.now());
        }
        aiJobRepository.save(entity);
    }

    @Override
    public AiJobDto findAiJob(UUID jobId) {
        var entity = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.AUTODUMP_AI_JOB_NOT_FOUND));
        return mapper.toAiJobDto(entity);
    }

    @Override
    public List<AiJobDto> findAiJobsBySession(UUID sessionId) {
        return aiJobRepository.findByImportSessionId(sessionId).stream()
                .map(mapper::toAiJobDto)
                .toList();
    }

    private String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
