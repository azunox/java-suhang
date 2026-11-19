package com.teamnote.common.dto;

import jakarta.validation.constraints.NotNull;

public record ContentPreviewRequest(
        @NotNull(message = "Markdown 원문은 필수입니다.")
        String markdown
) {
}
