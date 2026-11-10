package com.teamnote.task.dto;

import java.time.LocalDate;

import com.teamnote.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @NotBlank(message = "업무 제목은 필수입니다.")
        @Size(max = 120, message = "업무 제목은 120자 이하여야 합니다.")
        String title,
        @NotNull(message = "업무 설명은 필수입니다.")
        String descriptionMarkdown,
        @NotBlank(message = "담당자 이름은 필수입니다.")
        @Size(max = 80, message = "담당자 이름은 80자 이하여야 합니다.")
        String assigneeName,
        @NotNull(message = "마감일은 필수입니다.")
        LocalDate dueDate,
        @NotNull(message = "업무 상태는 필수입니다.")
        TaskStatus status
) {
}
