package kz.ask.chat.api.dto;

import jakarta.validation.constraints.AssertTrue;
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
public class SendMessageRequest {

    private String text;

    private String attachmentUrl;

    @AssertTrue(message = "Message text or attachment is required")
    public boolean hasContent() {
        return text != null && !text.isBlank()
                || attachmentUrl != null && !attachmentUrl.isBlank();
    }
}
