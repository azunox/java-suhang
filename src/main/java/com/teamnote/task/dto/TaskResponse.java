package com.teamnote.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.teamnote.task.entity.TaskStatus;

public record TaskResponse(
        Long id,
        Long projectId,
        String title,
        String descriptionMarkdown,
        String assigneeName,
        LocalDate dueDate,
        TaskStatus status,
        LocalDateTime completedAt
) {
}
