package kz.ask.autodump.domain;

import java.util.List;
import java.util.UUID;
import kz.ask.autodump.domain.dto.AiJobDto;
import kz.ask.autodump.domain.dto.ImportSessionDto;
import kz.ask.autodump.domain.dto.RawInputDto;
import kz.ask.autodump.domain.enums.AiJobStatus;
import kz.ask.autodump.domain.enums.ImportSessionStatus;
import kz.ask.autodump.domain.enums.SourceType;

public interface AutodumpImportService {

    ImportSessionDto createSession(UUID businessId, UUID branchId, UUID createdBy,
                                   SourceType sourceType, String inputSummary);

    ImportSessionDto findById(UUID sessionId);

    ImportSessionDto findByIdAndBranch(UUID sessionId, UUID branchId);

    List<ImportSessionDto> findByBranch(UUID branchId);

    void updateStatus(UUID sessionId, ImportSessionStatus status);

    void updateCounts(UUID sessionId, Integer totalDrafts, Integer approved, Integer rejected, Integer errors);

    RawInputDto storeRawInput(UUID sessionId, String originalFileName, String contentType, String rawText);

    AiJobDto createAiJob(UUID sessionId, UUID rawInputId, String provider, String model, String promptVersion);

    void updateAiJobStatus(UUID jobId, AiJobStatus status, String errorMessage);

    void completeAiJob(UUID jobId, String rawResponseJson, Integer inputTokens, Integer outputTokens);

    AiJobDto findAiJob(UUID jobId);

    List<AiJobDto> findAiJobsBySession(UUID sessionId);
}
