package com.teamnote.project.dto;

import java.time.LocalDate;

import com.teamnote.project.entity.ProjectStatus;

public record ProjectResponse(
        Long id,
        String name,
        String descriptionMarkdown,
        LocalDate startDate,
        LocalDate endDate,
        ProjectStatus status
) {
}
