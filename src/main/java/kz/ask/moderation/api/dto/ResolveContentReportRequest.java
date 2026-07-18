package kz.ask.moderation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.ask.moderation.domain.enums.ContentReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveContentReportRequest {

    @NotNull
    private ContentReportStatus status;

    @NotBlank
    private String resolution;
}
