package kz.ask.chat.domain.event;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChatFilesDeletionRequested {

    private final List<String> storedNames;
}
