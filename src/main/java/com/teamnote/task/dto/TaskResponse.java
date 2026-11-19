package com.teamnote.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.teamnote.task.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        String descriptionMarkdown,
        String descriptionHtml,
        String assigneeName,
        LocalDate dueDate,
        TaskStatus status,
        @Schema(
                implementation = String.class,
                types = {"string", "null"},
                description = "오프셋 없는 완료 시각. 미완료 업무는 null",
                example = "2026-11-19T10:00:00"
        )
        LocalDateTime completedAt
) {
}
