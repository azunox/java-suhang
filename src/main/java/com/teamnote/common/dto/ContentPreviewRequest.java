package com.teamnote.common.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;

public record ContentPreviewRequest(
        @NotNull(message = "Markdown 원문은 필수입니다.")
        @JsonDeserialize(using = StrictStringDeserializer.class)
        String markdown
) {
}
